package com.mj.mijing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mj.mijing.dto.Result;
import com.mj.mijing.entity.UserInfo;

public interface UserInfoService extends IService<UserInfo> {
    /** 查询用户详情 */
    Result queryUserInfoById(Long userId);

    /** 更新用户详情 */
    Result updateUserInfo(UserInfo userInfo);
}
