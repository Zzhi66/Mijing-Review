package com.mj.mijing.controller;

import com.mj.mijing.dto.LoginFormDTO;
import com.mj.mijing.dto.Result;
import com.mj.mijing.entity.UserInfo;
import com.mj.mijing.service.UserInfoService;
import com.mj.mijing.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private UserInfoService userInfoService;

    /** 发送验证码 */
    @PostMapping("code")
    public Result sendCode(@RequestParam("phone") String phone) {
        return userService.sendCode(phone);
    }

    /** 登录 */
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginForm) {
        return userService.login(loginForm);
    }

    /** 退出登录 */
    @PostMapping("/logout")
    public Result logout(HttpServletRequest request) {
        return userService.logout(request);
    }

    /** 获取当前用户信息 */
    @GetMapping("/me")
    public Result me() {
        return userService.me();
    }

    /** 根据 ID 查询用户 */
    @GetMapping("/{id}")
    public Result queryUserById(@PathVariable Long id) {
        return userService.queryUserById(id);
    }

    /** 查询用户详情 */
    @GetMapping("/info/{id}")
    public Result info(@PathVariable Long id) {
        return userInfoService.queryUserInfoById(id);
    }

    /** 更新用户详情 */
    @PutMapping("/info")
    public Result updateInfo(@RequestBody UserInfo userInfo) {
        return userInfoService.updateUserInfo(userInfo);
    }
}
