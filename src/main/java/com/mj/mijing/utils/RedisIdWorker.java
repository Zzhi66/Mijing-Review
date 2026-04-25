package com.mj.mijing.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的全局唯一 ID 生成器
 * 格式：1位符号位 + 31位时间戳（秒） + 32位序列号
 */
@Component
public class RedisIdWorker {

    /** 开始时间戳（2024-01-01 00:00:00 UTC） */
    private static final long BEGIN_TIMESTAMP = 1704067200L;

    /** 序列号位数 */
    private static final int COUNT_BITS = 32;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 生成全局唯一ID
     * @param keyPrefix 业务前缀
     */
    public long nextId(String keyPrefix) {
        // 生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;

        // 生成序列号（按天分key，防止超出Long范围）
        String date = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = stringRedisTemplate.opsForValue()
                .increment(RedisConstants.ID_ORDER_KEY + keyPrefix + ":" + date);

        return timestamp << COUNT_BITS | count;
    }
}
