package com.mj.mijing.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mj.mijing.ai.UserMemoryExtractor;
import com.mj.mijing.entity.UserMemory;
import com.mj.mijing.mapper.UserMemoryMapper;
import com.mj.mijing.service.UserMemoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户长期个性化记忆服务实现。
 */
@Slf4j
@Service
public class UserMemoryServiceImpl extends ServiceImpl<UserMemoryMapper, UserMemory> implements UserMemoryService {

    private static final String MEMORY_SOURCE_AI_CHAT = "ai_chat";
    private static final int MAX_CONTENT_LENGTH = 512;

    @Resource
    private UserMemoryExtractor userMemoryExtractor;

    /**
     * 异步提取并保存长期记忆，避免拖慢 AI 对话接口响应。
     */
    @Async
    @Override
    @Transactional
    public void extractAndSaveFromChat(Long userId, String userMessage, String aiResponse) {
        if (userId == null || userMessage == null || userMessage.isBlank()) {
            return;
        }

        List<UserMemoryExtractor.ExtractedMemory> extractedMemories =
                userMemoryExtractor.extract(userMessage, aiResponse);
        if (extractedMemories.isEmpty()) {
            return;
        }

        extractedMemories.forEach(memory -> saveOrRefreshMemory(userId, memory));
    }

    private void saveOrRefreshMemory(Long userId, UserMemoryExtractor.ExtractedMemory extractedMemory) {
        String memoryType = extractedMemory.getType().trim();
        String content = normalizeContent(extractedMemory.getContent());
        if (content.isEmpty()) {
            return;
        }

        String sourceHash = sha256(userId + ":" + memoryType + ":" + content);
        BigDecimal confidence = normalizeConfidence(extractedMemory.getConfidence());

        UserMemory existing = lambdaQuery()
                .eq(UserMemory::getUserId, userId)
                .eq(UserMemory::getSourceHash, sourceHash)
                .one();
        if (existing != null) {
            // 已有相同记忆时刷新置信度和更新时间，避免重复堆积。
            existing.setConfidence(confidence);
            existing.setEnabled(1);
            existing.setUpdateTime(LocalDateTime.now());
            updateById(existing);
            return;
        }

        UserMemory userMemory = new UserMemory();
        userMemory.setUserId(userId);
        userMemory.setMemoryType(memoryType);
        userMemory.setContent(content);
        userMemory.setConfidence(confidence);
        userMemory.setSource(MEMORY_SOURCE_AI_CHAT);
        userMemory.setSourceHash(sourceHash);
        userMemory.setEnabled(1);
        userMemory.setCreateTime(LocalDateTime.now());
        userMemory.setUpdateTime(LocalDateTime.now());

        try {
            save(userMemory);
        } catch (DuplicateKeyException e) {
            // 并发场景可能刚好被另一条异步任务写入，唯一索引会兜底去重。
            log.debug("用户记忆已存在，跳过重复写入：userId={}, hash={}", userId, sourceHash);
        }
    }

    private String normalizeContent(String content) {
        if (content == null) {
            return "";
        }
        String normalized = content.trim();
        if (normalized.length() > MAX_CONTENT_LENGTH) {
            return normalized.substring(0, MAX_CONTENT_LENGTH);
        }
        return normalized;
    }

    private BigDecimal normalizeConfidence(Double confidence) {
        double value = confidence == null ? 0.8D : confidence;
        value = Math.max(0D, Math.min(1D, value));
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception e) {
            throw new IllegalStateException("生成用户记忆哈希失败", e);
        }
    }
}
