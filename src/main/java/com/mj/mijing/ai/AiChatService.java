package com.mj.mijing.ai;

import com.mj.mijing.utils.RedisConstants;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import com.mj.mijing.dto.UserDTO;
import com.mj.mijing.utils.UserHolder;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * AI 智能客服服务
 * 特性：
 *   - 阿里云百炼（DashScope）qwen-turbo 模型
 *   - Redis List 存储多轮会话上下文（最近20条）
 *   - LangChain4j Function Calling 工具：商铺查询
 */
@Slf4j
@Service
public class AiChatService {

    @Value("${ai.dashscope.api-key:your-api-key-here}")
    private String apiKey;

    @Value("${ai.dashscope.model:qwen-turbo}")
    private String model;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ShopSearchTool shopSearchTool;

    @Resource
    private ShopOrderTool shopOrderTool;

    /**
     * 多轮对话（带会话记忆）
     * @param userId  用户 ID（会话隔离）
     * @param message 用户消息
     */
    public String chat(Long userId, String message) {
        // 1. 从 Redis 加载历史消息（最近20条，用于上下文）
        String historyKey = RedisConstants.AI_CHAT_HISTORY_KEY + userId;
        List<String> history = stringRedisTemplate.opsForList()
                .range(historyKey, 0, RedisConstants.AI_CHAT_HISTORY_MAX - 1);

        // 2. 构建会话上下文
        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("你是觅境点评的 AI 智能客服，专为用户提供高品质生活空间探索服务。")
                .append("你能帮用户搜索附近商铺、查看评价、了解优惠活动。保持友好简洁的风格。\n");
        if (history != null && !history.isEmpty()) {
            contextBuilder.append("以下是此前的对话记录：\n");
            history.forEach(h -> contextBuilder.append(h).append("\n"));
        }

        // 3. 调用模型（含 Function Calling 工具注册）
        String response;
        try {
            // 注入用户上下文，供 Tool 使用（主要用于下单逻辑）
            UserDTO user = new UserDTO();
            user.setId(userId);
            UserHolder.saveUser(user);

            ChatLanguageModel chatModel = QwenChatModel.builder()
                    .apiKey(apiKey)
                    .modelName(model)
                    .build();

            ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(
                    RedisConstants.AI_CHAT_HISTORY_MAX);

            MijingAiAssistant assistant = AiServices.builder(MijingAiAssistant.class)
                    .chatLanguageModel(chatModel)
                    .chatMemory(chatMemory)
                    .tools(shopSearchTool, shopOrderTool)
                    .build();

            response = assistant.chat(contextBuilder.toString() + "\n用户：" + message);
        } catch (Exception e) {
            log.error("AI 模型调用失败：{}", e.getMessage());
            response = "抱歉，AI 客服暂时不可用，请稍后再试。";
        } finally {
            // 防止 ThreadLocal 内存泄漏
            UserHolder.removeUser();
        }

        // 4. 更新 Redis 会话历史（LPUSH + LTRIM 保持最多20条）
        String userRecord = "用户：" + message;
        String aiRecord = "客服：" + response;
        stringRedisTemplate.opsForList().leftPush(historyKey, aiRecord);
        stringRedisTemplate.opsForList().leftPush(historyKey, userRecord);
        stringRedisTemplate.opsForList().trim(historyKey, 0, RedisConstants.AI_CHAT_HISTORY_MAX - 1);
        stringRedisTemplate.expire(historyKey, RedisConstants.AI_CHAT_HISTORY_TTL, TimeUnit.MINUTES);

        return response;
    }
}
