package com.mj.mijing.utils;

import com.github.benmanes.caffeine.cache.Cache;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Caffeine(L1) + Redis(L2) 二级缓存工具类
 * 读取优先级：Caffeine → Redis → DB
 * 更新：先更 DB，再双删 Redis 和 Caffeine
 */
@Slf4j
@Component
public class TwoLevelCacheClient {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource(name = "shopLocalCache")
    private Cache<String, String> localCache;

    /**
     * 查询（二级缓存）
     * @param key     缓存 Key
     * @param type    返回类型
     * @param db      DB 查询函数
     * @param redisTtl Redis 过期时间（分钟）
     */
    public <R> R get(String key, Class<R> type, Function<String, R> db, long redisTtl) {
        // L1：Caffeine
        String local = localCache.getIfPresent(key);
        if (StrUtil.isNotBlank(local)) {
            log.debug("命中 L1 Caffeine: {}", key);
            return JSONUtil.toBean(local, type);
        }
        // L2：Redis
        String redisVal = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(redisVal)) {
            log.debug("命中 L2 Redis: {}", key);
            localCache.put(key, redisVal); // 回填 L1
            return JSONUtil.toBean(redisVal, type);
        }
        if (redisVal != null) {
            return null; // 空值缓存（穿透防护）
        }
        // DB 查询
        R r = db.apply(key);
        String json = r == null ? "" : JSONUtil.toJsonStr(r);
        // 写入 L2
        stringRedisTemplate.opsForValue().set(key, json, redisTtl, TimeUnit.MINUTES);
        // 写入 L1
        if (r != null) {
            localCache.put(key, json);
        }
        return r;
    }

    /**
     * 更新缓存（写数据库后调用此方法双删）
     */
    public void invalidate(String key) {
        localCache.invalidate(key);
        stringRedisTemplate.delete(key);
        log.debug("缓存双删: {}", key);
    }
}
