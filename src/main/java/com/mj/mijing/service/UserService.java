package com.mj.mijing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mj.mijing.dto.LoginFormDTO;
import com.mj.mijing.dto.Result;
import com.mj.mijing.entity.User;

import javax.servlet.http.HttpServletRequest;

public interface UserService extends IService<User> {
    /** 发送手机验证码 */
    Result sendCode(String phone);

    /** 登录（验证码） */
    Result login(LoginFormDTO loginForm);

    /** 退出登录 */
    Result logout(HttpServletRequest request);

    /** 查询当前用户信息 */
    Result me();

    /** 根据 ID 查询用户 */
    Result queryUserById(Long userId);
}
