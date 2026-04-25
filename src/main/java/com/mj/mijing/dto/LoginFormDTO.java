package com.mj.mijing.dto;

import lombok.Data;

/**
 * 登录请求 DTO
 */
@Data
public class LoginFormDTO {
    /** 手机号 */
    private String phone;
    /** 验证码 */
    private String code;
    /** 密码（密码登录时使用） */
    private String password;
}
