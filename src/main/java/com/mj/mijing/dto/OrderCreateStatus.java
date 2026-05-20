package com.mj.mijing.dto;

/**
 * 秒杀订单异步创建状态
 */
public enum OrderCreateStatus {
    /** 资格抢占成功，订单创建受理中 */
    PENDING,
    /** 订单已落库 */
    SUCCESS,
    /** 创建失败（Kafka 投递失败 / DB 写入失败 / 死信补偿） */
    FAILED
}
