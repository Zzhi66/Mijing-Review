package com.mj.mijing.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mj.mijing.dto.LoginFormDTO;
import com.mj.mijing.dto.Result;
import com.mj.mijing.dto.UserDTO;
import com.mj.mijing.entity.User;
import com.mj.mijing.mapper.UserMapper;
import com.mj.mijing.service.UserService;
import com.mj.mijing.utils.RedisConstants;
import com.mj.mijing.utils.SystemConstants;
import com.mj.mijing.utils.UserHolder;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 用户 Service 实现
 * 功能：短信登录（验证码）、Redis Token 管理
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone) {
        // 手机号格式简单校验
        if (StrUtil.isBlank(phone) || !phone.matches("^1[3-9]\\d{9}$")) {
            return Result.fail("手机号格式错误");
        }
        // 生成6位验证码
        String code = RandomUtil.randomNumbers(6);
        // 存入 Redis（2分钟过期）
        stringRedisTemplate.opsForValue().set(
                RedisConstants.LOGIN_CODE_KEY + phone, code,
                RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);
        // 模拟发送（实际接入短信服务商）
        log.info("【觅境点评】验证码已发送：{}，验证码：{}", phone, code);
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm) {
        String phone = loginForm.getPhone();
        if (StrUtil.isBlank(phone) || !phone.matches("^1[3-9]\\d{9}$")) {
            return Result.fail("手机号格式错误");
        }
        // 校验验证码
        String cacheCode = stringRedisTemplate.opsForValue()
                .get(RedisConstants.LOGIN_CODE_KEY + phone);
        if (StrUtil.isBlank(cacheCode) || !cacheCode.equals(loginForm.getCode())) {
            return Result.fail("验证码错误或已过期");
        }
        // 查询用户，不存在则注册
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (user == null) {
            user = new User();
            user.setPhone(phone);
            user.setNickName(SystemConstants.USER_NICK_NAME_PREFIX + RandomUtil.randomString(6));
            save(user);
        }
        // 生成 Token 并存入 Redis
        String token = UUID.randomUUID().toString().replace("-", "");
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        stringRedisTemplate.opsForValue().set(
                RedisConstants.LOGIN_USER_KEY + token,
                JSONUtil.toJsonStr(userDTO),
                RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
        // 删除验证码
        stringRedisTemplate.delete(RedisConstants.LOGIN_CODE_KEY + phone);
        return Result.ok(token);
    }

    @Override
    public Result logout(HttpServletRequest request) {
        String token = request.getHeader("authorization");
        if (StrUtil.isNotBlank(token)) {
            stringRedisTemplate.delete(RedisConstants.LOGIN_USER_KEY + token);
        }
        UserHolder.removeUser();
        return Result.ok();
    }

    @Override
    public Result me() {
        UserDTO user = UserHolder.getUser();
        return Result.ok(user);
    }

    @Override
    public Result queryUserById(Long userId) {
        User user = getById(userId);
        if (user == null) {
            return Result.ok();
        }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        return Result.ok(userDTO);
    }
}
