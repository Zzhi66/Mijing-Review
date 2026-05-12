package com.mj.mijing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户长期个性化记忆实体。
 * 用于沉淀 AI 会话中提取出的稳定偏好和事实，和 Redis 短期上下文分层存储。
 */
@Data
@TableName("tb_user_memory")
public class UserMemory {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    /**
     * 记忆类型：preference 表示偏好，fact 表示事实。
     */
    private String memoryType;

    private String content;

    /**
     * LLM 提取该条记忆的置信度。
     */
    private BigDecimal confidence;

    private String source;

    /**
     * 由用户、类型、内容生成的哈希，用于避免重复写入相同记忆。
     */
    private String sourceHash;

    private Integer enabled;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
