package com.mj.mijing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mj.mijing.dto.Result;
import com.mj.mijing.entity.Follow;

public interface FollowService extends IService<Follow> {
    /** 关注/取关 */
    Result follow(Long followUserId, Boolean isFollow);

    /** 是否已关注 */
    Result isFollow(Long followUserId);

    /** 共同关注 */
    Result followCommons(Long id);
}
