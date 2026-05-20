package com.mj.mijing.utils;

import com.mj.mijing.dto.OrderCreateStatus;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀订单创建状态 Redis 读写封装
 */
@Component
public class OrderStatusRedisHelper {

    private static final String FIELD_STATUS = "status";
    private static final String FIELD_USER_ID = "userId";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public void markPending(Long orderId, Long userId) {
        writeStatus(orderId, userId, OrderCreateStatus.PENDING);
    }

    public void markSuccess(Long orderId, Long userId) {
        writeStatus(orderId, userId, OrderCreateStatus.SUCCESS);
    }

    public void markFailed(Long orderId, Long userId) {
        writeStatus(orderId, userId, OrderCreateStatus.FAILED);
    }

    /**
     * @return status 与 userId，Key 不存在时返回 null
     */
    public Map<String, String> getStatus(Long orderId) {
        String key = statusKey(orderId);
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
        if (entries == null || entries.isEmpty()) {
            return null;
        }
        return Map.of(
                FIELD_STATUS, String.valueOf(entries.get(FIELD_STATUS)),
                FIELD_USER_ID, String.valueOf(entries.get(FIELD_USER_ID))
        );
    }

    private void writeStatus(Long orderId, Long userId, OrderCreateStatus status) {
        String key = statusKey(orderId);
        stringRedisTemplate.opsForHash().put(key, FIELD_STATUS, status.name());
        stringRedisTemplate.opsForHash().put(key, FIELD_USER_ID, String.valueOf(userId));
        stringRedisTemplate.expire(key, RedisConstants.ORDER_STATUS_TTL, TimeUnit.SECONDS);
    }

    private String statusKey(Long orderId) {
        return SystemConstants.ORDER_STATUS_KEY_PREFIX + orderId;
    }
}
