package com.mj.mijing.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mj.mijing.entity.SeckillVoucher;
import com.mj.mijing.mapper.SeckillVoucherMapper;
import com.mj.mijing.service.SeckillVoucherService;
import com.mj.mijing.utils.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 秒杀优惠券 Service 实现
 * <p>
 * 职责：
 *   1. 管理秒杀券的库存扣减与回滚（乐观锁 + Redis 双写）
 *   2. 校验秒杀时间窗口
 *   3. 继承 MyBatis-Plus ServiceImpl 提供基础 CRUD
 */
@Slf4j
@Service
public class SeckillVoucherServiceImpl extends ServiceImpl<SeckillVoucherMapper, SeckillVoucher>
        implements SeckillVoucherService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 扣减数据库库存（乐观锁：stock > 0 才扣减）
     * 注意：因为使用了 Lua 脚本做 Redis 预减库存，所以这里作为 MQ 消费者异步落库时，
     * 绝对不能再次去扣减 Redis 库存，否则会导致 Redis 库存被扣减两次！
     */
    @Override
    @Transactional
    public boolean deductStock(Long voucherId) {
        boolean success = update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId)
                .gt("stock", 0)
                .update();
        if (success) {
            log.debug("数据库库存扣减成功，voucherId={}", voucherId);
        } else {
            log.warn("数据库库存扣减失败（乐观锁），voucherId={}", voucherId);
        }
        return success;
    }

    /**
     * 回滚库存（订单取消/退款时恢复）
     * 同时恢复 Redis 中的库存缓存
     */
    @Override
    @Transactional
    public void rollbackStock(Long voucherId) {
        boolean success = update()
                .setSql("stock = stock + 1")
                .eq("voucher_id", voucherId)
                .update();
        if (success) {
            // 同步恢复 Redis 库存
            stringRedisTemplate.opsForValue().increment(RedisConstants.SECKILL_STOCK_KEY + voucherId);
            log.info("库存回滚成功，voucherId={}", voucherId);
        } else {
            log.error("库存回滚失败，voucherId={}", voucherId);
        }
    }

    /**
     * 判断秒杀是否在有效时间窗口内
     */
    @Override
    public boolean isWithinSeckillTime(Long voucherId) {
        SeckillVoucher voucher = getById(voucherId);
        if (voucher == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(voucher.getBeginTime()) && !now.isAfter(voucher.getEndTime());
    }
}
