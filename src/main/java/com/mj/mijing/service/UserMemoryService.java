package com.mj.mijing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mj.mijing.entity.UserMemory;

/**
 * 用户长期个性化记忆服务。
 */
public interface UserMemoryService extends IService<UserMemory> {

    /**
     * 从单轮 AI 对话中提取用户偏好和事实，并沉淀到数据库。
     *
     * @param userId 用户 ID
     * @param userMessage 用户原始消息
     * @param aiResponse AI 回复
     */
    void extractAndSaveFromChat(Long userId, String userMessage, String aiResponse);
}
