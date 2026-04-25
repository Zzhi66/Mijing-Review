package com.mj.mijing.kafka;

import com.mj.mijing.utils.RedisConstants;
import com.mj.mijing.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 秒杀订单死信队列（DLT）消费者
 * <p>
 * 当主消费者重试 3 次仍失败后，消息进入此队列。
 * 处理策略：回滚 Redis 库存和已购记录，记录告警日志（可对接告警平台）
 */
@Slf4j
@Component
public class VoucherOrderDltConsumer {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @KafkaListener(topics = SystemConstants.TOPIC_VOUCHER_ORDER_DLT, groupId = "mijing-dlt-group")
    public void onDeadLetterMessage(VoucherOrderMessage msg) {
        log.error("【死信队列】收到消费失败的秒杀订单，执行补偿回滚，orderId={}, userId={}, voucherId={}",
                msg.getOrderId(), msg.getUserId(), msg.getVoucherId());

        try {
            // 回滚 Redis 库存（+1）
            String stockKey = RedisConstants.SECKILL_STOCK_KEY + msg.getVoucherId();
            stringRedisTemplate.opsForValue().increment(stockKey);

            // 移除已购记录（允许用户重新购买）
            String orderKey = SystemConstants.SECKILL_ORDER_KEY_PREFIX + msg.getVoucherId();
            stringRedisTemplate.opsForSet().remove(orderKey, String.valueOf(msg.getUserId()));

            log.warn("【死信队列】Redis 补偿回滚完成，voucherId={}, userId={}",
                    msg.getVoucherId(), msg.getUserId());

            // TODO: 对接告警平台（钉钉/企微/邮件），通知运维人员
            // alertService.sendAlert("秒杀订单落库失败", msg);
        } catch (Exception e) {
            // 补偿回滚本身也失败了，只能依赖定时对账兜底
            log.error("【死信队列】Redis 补偿回滚异常，需人工介入，orderId={}, error={}",
                    msg.getOrderId(), e.getMessage(), e);
        }
    }
}
