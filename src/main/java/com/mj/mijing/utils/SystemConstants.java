package com.mj.mijing.utils;

/**
 * 系统常量
 */
public class SystemConstants {

    /** 默认昵称前缀 */
    public static final String USER_NICK_NAME_PREFIX = "觅客_";

    /** 探店笔记分页大小 */
    public static final int DEFAULT_PAGE_SIZE = 5;
    public static final int MAX_PAGE_SIZE = 10;

    /** 附近商铺查询默认半径（公里） */
    public static final double DEFAULT_GEO_RADIUS_KM = 3.0;

    /** 订单超时未支付关闭时间（分钟） */
    public static final int ORDER_TIMEOUT_MINUTES = 30;

    /** Kafka Topic 名称 */
    public static final String TOPIC_VOUCHER_ORDER = "mj.voucher.order";

    /** Kafka 死信 Topic（消费失败的消息进入此队列）*/
    public static final String TOPIC_VOUCHER_ORDER_DLT = "mj.voucher.order.DLT";

    /** 秒杀已购用户集合 Key 前缀 */
    public static final String SECKILL_ORDER_KEY_PREFIX = "mj:seckill:order:";

    /** Caffeine 本地缓存最大条目数 */
    public static final int CAFFEINE_MAX_SIZE = 500;
    public static final long CAFFEINE_EXPIRE_MINUTES = 10L;
}
