package com.mj.mijing.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mj.mijing.dto.OrderCreateStatus;
import com.mj.mijing.dto.Result;
import com.mj.mijing.dto.SeckillOrderStatusVO;
import com.mj.mijing.entity.SeckillVoucher;
import com.mj.mijing.entity.VoucherOrder;
import com.mj.mijing.kafka.VoucherOrderMessage;
import com.mj.mijing.mapper.VoucherOrderMapper;
import com.mj.mijing.service.SeckillVoucherService;
import com.mj.mijing.service.VoucherOrderService;
import com.mj.mijing.utils.OrderStatusRedisHelper;
import com.mj.mijing.utils.RedisConstants;
import com.mj.mijing.utils.RedisIdWorker;
import com.mj.mijing.utils.SystemConstants;
import com.mj.mijing.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

/**
 * 优惠券订单 Service 实现
 * 核心流程：
 *   1. Lua 脚本原子校验库存 + 一人一单
 *   2. 发送 Kafka 消息（流量削峰）
 *   3. Kafka 消费者异步写 DB + 乐观锁控制并发
 */
@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder>
        implements VoucherOrderService {

    @Resource
    private SeckillVoucherService seckillVoucherService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private KafkaTemplate<String, VoucherOrderMessage> kafkaTemplate;

    @Resource
    private OrderStatusRedisHelper orderStatusRedisHelper;

    /** 秒杀 Lua 脚本 */
    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("scripts/seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    /**
     * 秒杀下单（高并发入口）
     * Lua 脚本原子校验 → Kafka 削峰 → 异步创建订单。
     * 返回的 orderId 为关联 ID，表示资格抢占成功、订单创建受理中，不表示 DB 已落库。
     */
    @Override
    public Result seckillVoucher(Long voucherId) {
        // 1. 校验秒杀时间窗口
        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
        if (voucher == null) {
            return Result.fail("优惠券不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(voucher.getBeginTime())) {
            return Result.fail("秒杀尚未开始");
        }
        if (now.isAfter(voucher.getEndTime())) {
            return Result.fail("秒杀已结束");
        }

        Long userId = UserHolder.getUser().getId();
        long orderId = redisIdWorker.nextId("order");

        // 2. 执行 Lua 脚本（原子性：校验库存 + 一人一单 + 扣库存）
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Arrays.asList(
                        RedisConstants.SECKILL_STOCK_KEY + voucherId,
                        SystemConstants.SECKILL_ORDER_KEY_PREFIX + voucherId
                ),
                String.valueOf(userId),
                String.valueOf(orderId)
        );

        int r = result == null ? -1 : result.intValue();
        if (r == 1) {
            return Result.fail("库存不足");
        }
        if (r == 2) {
            return Result.fail("每人限购一张");
        }

        // 3. 标记订单创建受理中
        orderStatusRedisHelper.markPending(orderId, userId);

        // 4. 发送 Kafka 消息（异步处理，流量削峰）+ 回调补偿
        VoucherOrderMessage msg = new VoucherOrderMessage(orderId, userId, voucherId);
        String stockKey = RedisConstants.SECKILL_STOCK_KEY + voucherId;
        String orderKey = SystemConstants.SECKILL_ORDER_KEY_PREFIX + voucherId;

        kafkaTemplate.send(SystemConstants.TOPIC_VOUCHER_ORDER, String.valueOf(userId), msg)
                .addCallback(
                        success -> log.info("秒杀订单消息投递成功，orderId={}, userId={}, voucherId={}",
                                orderId, userId, voucherId),
                        failure -> {
                            // Kafka 发送失败（重试耗尽），回滚 Redis 库存与已购记录
                            log.error("秒杀订单消息投递失败，执行 Redis 补偿回滚，orderId={}, voucherId={}, error={}",
                                    orderId, voucherId, failure.getMessage());
                            stringRedisTemplate.opsForValue().increment(stockKey);
                            stringRedisTemplate.opsForSet().remove(orderKey, String.valueOf(userId));
                            orderStatusRedisHelper.markFailed(orderId, userId);
                        }
                );

        return Result.ok(orderId);
    }

    /**
     * 异步创建订单（Kafka 消费者调用）
     * 使用 Redisson 分布式锁保证一人一单；乐观锁（@Version）防止并发冲突
     */
    @Override
    @Transactional
    public void createVoucherOrder(VoucherOrder voucherOrder) {
        Long orderId = voucherOrder.getId();
        Long userId = voucherOrder.getUserId();
        Long voucherId = voucherOrder.getVoucherId();

        // Redisson 分布式锁（幂等兜底）
        RLock lock = redissonClient.getLock("mj:lock:order:" + userId);
        boolean isLock = lock.tryLock();
        if (!isLock) {
            log.warn("重复下单请求被拒绝，userId={}", userId);
            resolveStatusAfterFailure(orderId, userId);
            return;
        }
        try {
            // DB 层幂等校验
            int count = query().eq("user_id", userId).eq("voucher_id", voucherId).count().intValue();
            if (count > 0) {
                log.warn("用户已购买，userId={}, voucherId={}", userId, voucherId);
                orderStatusRedisHelper.markSuccess(orderId, userId);
                return;
            }
            // 扣减库存（乐观锁：stock > 0，同步扣减 Redis 缓存）
            boolean success = seckillVoucherService.deductStock(voucherId);
            if (!success) {
                log.warn("库存扣减失败（乐观锁），voucherId={}", voucherId);
                rollbackRedis(userId, voucherId);
                orderStatusRedisHelper.markFailed(orderId, userId);
                return;
            }
            // 保存订单
            try {
                save(voucherOrder);
                log.info("订单创建成功，orderId={}", voucherOrder.getId());
                orderStatusRedisHelper.markSuccess(orderId, userId);
            } catch (Exception e) {
                log.error("订单保存异常，执行 Redis 补偿回滚，userId={}, voucherId={}, error={}",
                        userId, voucherId, e.getMessage());
                rollbackRedis(userId, voucherId);
                orderStatusRedisHelper.markFailed(orderId, userId);
                throw e;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Result getSeckillOrderStatus(Long orderId) {
        Long currentUserId = UserHolder.getUser().getId();
        Map<String, String> cached = orderStatusRedisHelper.getStatus(orderId);

        if (cached != null) {
            Long ownerId = Long.parseLong(cached.get("userId"));
            if (!currentUserId.equals(ownerId)) {
                return Result.fail("无权查询该订单");
            }
            OrderCreateStatus status = OrderCreateStatus.valueOf(cached.get("status"));
            return Result.ok(buildStatusVO(orderId, status, null));
        }

        VoucherOrder order = getById(orderId);
        if (order != null) {
            if (!currentUserId.equals(order.getUserId())) {
                return Result.fail("无权查询该订单");
            }
            return Result.ok(buildStatusVO(orderId, OrderCreateStatus.SUCCESS, null));
        }

        return Result.ok(buildStatusVO(orderId, OrderCreateStatus.PENDING, "订单创建中，请稍候"));
    }

    private void resolveStatusAfterFailure(Long orderId, Long userId) {
        VoucherOrder existing = getById(orderId);
        if (existing != null && userId.equals(existing.getUserId())) {
            orderStatusRedisHelper.markSuccess(orderId, userId);
        } else {
            orderStatusRedisHelper.markFailed(orderId, userId);
        }
    }

    private SeckillOrderStatusVO buildStatusVO(Long orderId, OrderCreateStatus status, String message) {
        return new SeckillOrderStatusVO(orderId, status, message);
    }

    /**
     * 回滚 Redis 中 Lua 脚本已执行的操作：
     *   - 恢复秒杀库存计数
     *   - 移除用户已购记录
     * 适用于 Kafka 消费端 DB 写入失败的补偿场景
     */
    private void rollbackRedis(Long userId, Long voucherId) {
        String stockKey = RedisConstants.SECKILL_STOCK_KEY + voucherId;
        String orderKey = SystemConstants.SECKILL_ORDER_KEY_PREFIX + voucherId;
        stringRedisTemplate.opsForValue().increment(stockKey);                        // 恢复库存
        stringRedisTemplate.opsForSet().remove(orderKey, String.valueOf(userId));     // 移除购买记录
        log.warn("Redis 补偿回滚完成，userId={}, voucherId={}", userId, voucherId);
    }
}
