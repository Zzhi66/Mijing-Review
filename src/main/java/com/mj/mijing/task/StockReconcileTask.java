package com.mj.mijing.task;

import com.mj.mijing.entity.SeckillVoucher;
import com.mj.mijing.service.SeckillVoucherService;
import com.mj.mijing.utils.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 库存定时对账任务
 * <p>
 * 定期扫描所有秒杀券，对比 Redis 库存和 DB 库存，修正不一致。
 * 作为分布式事务最终一致性的最后一道兜底防线。
 * <p>
 * 执行频率：每天凌晨 3 点
 */
@Slf4j
@Component
public class StockReconcileTask {

    @Resource
    private SeckillVoucherService seckillVoucherService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 定时对账：Redis 库存 ↔ DB 库存
     * 以 DB 为准，修正 Redis 中的偏差
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void reconcileStock() {
        log.info("====== 库存对账任务开始 ======");
        int fixedCount = 0;

        // 查询所有秒杀券
        List<SeckillVoucher> vouchers = seckillVoucherService.list();
        for (SeckillVoucher voucher : vouchers) {
            Long voucherId = voucher.getVoucherId();
            int dbStock = voucher.getStock();
            String redisKey = RedisConstants.SECKILL_STOCK_KEY + voucherId;

            // 获取 Redis 中的库存
            String redisStockStr = stringRedisTemplate.opsForValue().get(redisKey);

            // Redis 中没有这个 key（可能已过期），跳过
            if (redisStockStr == null) {
                log.debug("对账跳过：voucherId={}, Redis key 不存在（可能未开始或已过期）", voucherId);
                continue;
            }

            int redisStock;
            try {
                redisStock = Integer.parseInt(redisStockStr);
            } catch (NumberFormatException e) {
                log.error("对账异常：voucherId={}, Redis 值非法: {}", voucherId, redisStockStr);
                // 强制修正为 DB 值
                stringRedisTemplate.opsForValue().set(redisKey, String.valueOf(dbStock));
                fixedCount++;
                continue;
            }

            // 比较并修正
            if (redisStock != dbStock) {
                log.warn("库存不一致！voucherId={}, Redis库存={}, DB库存={}, 以DB为准修正",
                        voucherId, redisStock, dbStock);
                stringRedisTemplate.opsForValue().set(redisKey, String.valueOf(dbStock));
                fixedCount++;
            } else {
                log.debug("库存一致：voucherId={}, stock={}", voucherId, dbStock);
            }
        }

        log.info("====== 库存对账任务完成：共检查 {} 个秒杀券，修正 {} 个不一致 ======",
                vouchers.size(), fixedCount);
    }
}
