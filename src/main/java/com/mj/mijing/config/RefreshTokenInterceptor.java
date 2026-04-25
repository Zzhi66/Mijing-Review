package com.mj.mijing.config;

import cn.hutool.core.util.StrUtil;
import com.mj.mijing.dto.UserDTO;
import com.mj.mijing.utils.RedisConstants;
import com.mj.mijing.utils.UserHolder;
import cn.hutool.json.JSONUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * 刷新 Token 拦截器（所有请求）
 * 只做 Token 刷新，不校验登录状态
 */
public class RefreshTokenInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;

    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("authorization");
        if (StrUtil.isBlank(token)) {
            return true;
        }
        String key = RedisConstants.LOGIN_USER_KEY + token;
        String userJson = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(userJson)) {
            return true;
        }
        UserDTO userDTO = JSONUtil.toBean(userJson, UserDTO.class);
        UserHolder.saveUser(userDTO);
        // 刷新 Token 过期时间
        stringRedisTemplate.expire(key, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserHolder.removeUser();
    }
}
