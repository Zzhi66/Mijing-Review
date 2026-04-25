package com.mj.mijing.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mj.mijing.utils.SystemConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine 本地缓存配置（L1 缓存）
 */
@Configuration
public class CaffeineConfig {

    /**
     * 商铺本地缓存
     */
    @Bean("shopLocalCache")
    public Cache<String, String> shopLocalCache() {
        return Caffeine.newBuilder()
                .maximumSize(SystemConstants.CAFFEINE_MAX_SIZE)
                .expireAfterWrite(SystemConstants.CAFFEINE_EXPIRE_MINUTES, TimeUnit.MINUTES)
                .build();
    }
}
