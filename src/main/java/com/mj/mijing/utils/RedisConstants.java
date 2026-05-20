package com.mj.mijing.utils;

/**
 * Redis Key 常量
 */
public class RedisConstants {

    // 登录验证码
    public static final String LOGIN_CODE_KEY = "mj:login:code:";
    public static final Long LOGIN_CODE_TTL = 2L; // 分钟

    // 用户登录 Token
    public static final String LOGIN_USER_KEY = "mj:login:token:";
    public static final Long LOGIN_USER_TTL = 30L; // 分钟

    // 商铺缓存（Caffeine+Redis二级）
    public static final String CACHE_SHOP_KEY = "mj:cache:shop:";
    public static final Long CACHE_SHOP_TTL = 30L; // 分钟
    public static final Long CACHE_NULL_TTL = 2L; // 分钟，空值缓存

    // 商铺类型缓存
    public static final String CACHE_SHOP_TYPE_KEY = "mj:cache:shop:type";
    public static final Long CACHE_SHOP_TYPE_TTL = 60L; // 分钟

    // 逻辑过期缓存（缓存击穿 - 热点数据）
    public static final String CACHE_SHOP_LOGICAL_KEY = "mj:cache:logical:shop:";
    public static final Long CACHE_SHOP_LOGICAL_TTL = 20L; // 秒（逻辑过期时间）

    // 分布式锁
    public static final String LOCK_SHOP_KEY = "mj:lock:shop:";
    public static final Long LOCK_SHOP_TTL = 10L; // 秒

    // 全局唯一ID（订单）
    public static final String ID_ORDER_KEY = "mj:incr:order:";

    // 秒杀库存
    public static final String SECKILL_STOCK_KEY = "mj:seckill:stock:";

    // 探店笔记点赞（ZSet，按时间排序）
    public static final String BLOG_LIKED_KEY = "mj:blog:liked:";

    // 关注推送 Feed 流收件箱
    public static final String FEED_KEY = "mj:feed:";

    // 商铺 GEO 数据
    public static final String SHOP_GEO_KEY = "mj:geo:shop:";

    // 用户签到 BitMap
    public static final String USER_SIGN_KEY = "mj:sign:";

    // AI 多轮会话记忆（List）
    public static final String AI_CHAT_HISTORY_KEY = "mj:chat:history:";
    public static final int AI_CHAT_HISTORY_MAX = 20; // 最多保留20条
    public static final Long AI_CHAT_HISTORY_TTL = 60L; // 分钟

    // 缓存一致性：延迟删除标记
    public static final String CACHE_DELETE_LOCK_KEY = "mj:cache:del:";

    // 秒杀订单创建状态（PENDING/SUCCESS/FAILED）
    public static final Long ORDER_STATUS_TTL = 600L; // 秒
}
