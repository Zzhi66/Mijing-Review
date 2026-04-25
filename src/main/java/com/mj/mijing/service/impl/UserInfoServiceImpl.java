package com.mj.mijing.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mj.mijing.dto.Result;
import com.mj.mijing.entity.UserInfo;
import com.mj.mijing.mapper.UserInfoMapper;
import com.mj.mijing.service.UserInfoService;
import com.mj.mijing.utils.UserHolder;
import org.springframework.stereotype.Service;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Override
    public Result queryUserInfoById(Long userId) {
        UserInfo userInfo = getById(userId);
        if (userInfo == null) {
            return Result.ok();
        }
        return Result.ok(userInfo);
    }

    @Override
    public Result updateUserInfo(UserInfo userInfo) {
        Long userId = UserHolder.getUser().getId();
        userInfo.setUserId(userId);
        updateById(userInfo);
        return Result.ok();
    }
}
