package com.mj.mijing.task;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mj.mijing.entity.VoucherOrder;
import com.mj.mijing.service.VoucherOrderService;
import com.mj.mijing.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * Spring Task 定时任务：超时未支付订单自动关单
 * 每分钟扫描，关闭 30 分钟前创建且状态为"待支付"的订单，回复库存
 */
@Slf4j
@Component
public class VoucherOrderTimeoutTask {

    @Resource
    private VoucherOrderService voucherOrderService;

    /**
     * 每分钟执行一次，关闭超时未支付订单
     */
    @Scheduled(cron = "0 * * * * ?")
    public void closeTimeoutOrders() {
        LocalDateTime timeout = LocalDateTime.now()
                .minusMinutes(SystemConstants.ORDER_TIMEOUT_MINUTES);
        // 查询超时待支付订单
        voucherOrderService.list(
                new LambdaUpdateWrapper<VoucherOrder>()
                        .eq(VoucherOrder::getStatus, 1) // 1=待支付
                        .lt(VoucherOrder::getCreateTime, timeout)
        ).forEach(order -> {
            // 关闭订单（状态改为4-已取消）
            voucherOrderService.update(
                    new LambdaUpdateWrapper<VoucherOrder>()
                            .eq(VoucherOrder::getId, order.getId())
                            .eq(VoucherOrder::getVersion, order.getVersion()) // 乐观锁
                            .set(VoucherOrder::getStatus, 4)
            );
            log.info("超时订单已关闭：orderId={}", order.getId());
        });
    }
}
