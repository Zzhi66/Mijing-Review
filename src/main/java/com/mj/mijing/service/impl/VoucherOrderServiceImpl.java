package com.mj.mijing.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mj.mijing.dto.Result;
import com.mj.mijing.entity.SeckillVoucher;
import com.mj.mijing.entity.VoucherOrder;
import com.mj.mijing.kafka.VoucherOrderMessage;
import com.mj.mijing.mapper.VoucherOrderMapper;
import com.mj.mijing.service.SeckillVoucherService;
import com.mj.mijing.service.VoucherOrderService;
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
import java.util.List;

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

    /** 秒杀 Lua 脚本 */
    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("scripts/seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    /**
     * 秒杀下单（高并发入口）
     * Lua 脚本原子校验 → Kafka 削峰 → 异步创建订单
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

        // 3. 发送 Kafka 消息（异步处理，流量削峰）+ 回调补偿
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
                        }
                );

        // 快速返回订单 ID（异步创建中）
        return Result.ok(orderId);
    }

    /**
     * 异步创建订单（Kafka 消费者调用）
     * 使用 Redisson 分布式锁保证一人一单；乐观锁（@Version）防止并发冲突
     */
    @Override
    @Transactional
    public void createVoucherOrder(VoucherOrder voucherOrder) {
        Long userId = voucherOrder.getUserId();
        Long voucherId = voucherOrder.getVoucherId();

        // Redisson 分布式锁（幂等兜底）
        RLock lock = redissonClient.getLock("mj:lock:order:" + userId);
        boolean isLock = lock.tryLock();
        if (!isLock) {
            log.warn("重复下单请求被拒绝，userId={}", userId);
            return;
        }
        try {
            // DB 层幂等校验
            int count = query().eq("user_id", userId).eq("voucher_id", voucherId).count().intValue();
            if (count > 0) {
                log.warn("用户已购买，userId={}, voucherId={}", userId, voucherId);
                return;
            }
            // 扣减库存（乐观锁：stock > 0，同步扣减 Redis 缓存）
            boolean success = seckillVoucherService.deductStock(voucherId);
            if (!success) {
                log.warn("库存扣减失败（乐观锁），voucherId={}", voucherId);
                // 乐观锁失败：DB 未写入，回滚 Redis 已扣减的库存与购买记录
                rollbackRedis(userId, voucherId);
                return;
            }
            // 保存订单
            try {
                save(voucherOrder);
                log.info("订单创建成功，orderId={}", voucherOrder.getId());
            } catch (Exception e) {
                // DB 写入失败：@Transactional 会回滚 DB，此处手动回滚 Redis
                log.error("订单保存异常，执行 Redis 补偿回滚，userId={}, voucherId={}, error={}",
                        userId, voucherId, e.getMessage());
                rollbackRedis(userId, voucherId);
                throw e; // 重新抛出，触发 @Transactional 回滚 deductStock 的 DB 操作
            }
        } finally {
            lock.unlock();
        }
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
