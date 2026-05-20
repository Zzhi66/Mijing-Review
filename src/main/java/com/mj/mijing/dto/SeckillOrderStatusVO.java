package com.mj.mijing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 秒杀订单创建状态查询响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillOrderStatusVO {
    private Long orderId;
    private OrderCreateStatus status;
    private String message;
}
