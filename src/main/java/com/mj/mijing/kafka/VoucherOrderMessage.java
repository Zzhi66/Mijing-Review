package com.mj.mijing.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 秒杀订单 Kafka 消息体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoucherOrderMessage {
    private Long orderId;
    private Long userId;
    private Long voucherId;
}
