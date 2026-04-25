package com.mj.mijing.kafka;

import com.mj.mijing.entity.VoucherOrder;
import com.mj.mijing.service.VoucherOrderService;
import com.mj.mijing.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 秒杀订单 Kafka 消费者
 * 异步接收消息 → 创建订单 → 扣减 DB 库存
 */
@Slf4j
@Component
public class VoucherOrderConsumer {

    @Resource
    private VoucherOrderService voucherOrderService;

    @KafkaListener(topics = SystemConstants.TOPIC_VOUCHER_ORDER, groupId = "mijing-group")
    public void onMessage(VoucherOrderMessage msg) {
        log.info("消费秒杀订单消息：orderId={}, userId={}, voucherId={}",
                msg.getOrderId(), msg.getUserId(), msg.getVoucherId());
        VoucherOrder order = new VoucherOrder();
        order.setId(msg.getOrderId());
        order.setUserId(msg.getUserId());
        order.setVoucherId(msg.getVoucherId());
        order.setPayType(1);
        order.setStatus(1);
        voucherOrderService.createVoucherOrder(order);
    }
}
