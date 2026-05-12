package com.mj.mijing.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户长期记忆提取器。
 * 负责调用 LLM 将一轮会话压缩成可持久化的偏好和事实。
 */
@Slf4j
@Component
public class UserMemoryExtractor {

    private static final String DEFAULT_API_KEY = "your-api-key-here";

    @Value("${ai.dashscope.api-key:your-api-key-here}")
    private String apiKey;

    @Value("${ai.dashscope.model:qwen-turbo}")
    private String model;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 从用户消息和 AI 回复中提取长期记忆。
     */
    public List<ExtractedMemory> extract(String userMessage, String aiResponse) {
        if (apiKey == null || apiKey.isBlank() || DEFAULT_API_KEY.equals(apiKey)) {
            log.warn("未配置 DashScope API Key，跳过用户记忆提取");
            return Collections.emptyList();
        }

        try {
            ChatLanguageModel chatModel = QwenChatModel.builder()
                    .apiKey(apiKey)
                    .modelName(model)
                    .build();

            MemoryExtractionAssistant assistant = AiServices.builder(MemoryExtractionAssistant.class)
                    .chatLanguageModel(chatModel)
                    .build();

            String rawResult = assistant.extract(buildPrompt(userMessage, aiResponse));
            MemoryExtractionResult result = objectMapper.readValue(extractJsonObject(rawResult), MemoryExtractionResult.class);
            if (result.getMemories() == null || result.getMemories().isEmpty()) {
                return Collections.emptyList();
            }

            return result.getMemories().stream()
                    .filter(this::isValidMemory)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // 记忆提取是增强能力，失败时只记录日志，不能影响主聊天链路。
            log.warn("用户记忆提取失败：{}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private String buildPrompt(String userMessage, String aiResponse) {
        return "你是觅境点评的用户画像分析助手，请从一轮用户与AI客服的对话中提取可用于后续生活空间推荐的长期记忆。\n"
                + "只提取相对稳定、与推荐相关的信息，忽略一次性闲聊、临时请求、手机号、支付信息、精确住址等敏感隐私。\n"
                + "记忆类型只能是 preference 或 fact。preference 表示用户偏好，fact 表示用户事实。\n"
                + "如果没有值得保存的记忆，返回 {\"memories\":[]}。\n"
                + "必须只输出 JSON，不要输出 Markdown、解释或代码块。\n"
                + "JSON 格式：{\"memories\":[{\"type\":\"preference\",\"content\":\"用户偏好安静的咖啡馆\",\"confidence\":0.9}]}\n\n"
                + "用户消息：\n" + userMessage + "\n\n"
                + "AI回复：\n" + aiResponse;
    }

    private String extractJsonObject(String rawResult) {
        if (rawResult == null) {
            return "{\"memories\":[]}";
        }
        String trimmed = rawResult.trim();
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end >= start) {
            return trimmed.substring(start, end + 1);
        }
        return "{\"memories\":[]}";
    }

    private boolean isValidMemory(ExtractedMemory memory) {
        if (memory == null || memory.getContent() == null || memory.getType() == null) {
            return false;
        }
        String type = memory.getType().trim();
        String content = memory.getContent().trim();
        return ("preference".equals(type) || "fact".equals(type)) && !content.isEmpty();
    }

    private interface MemoryExtractionAssistant {
        String extract(String prompt);
    }

    @Data
    public static class MemoryExtractionResult {
        private List<ExtractedMemory> memories;
    }

    @Data
    public static class ExtractedMemory {
        private String type;
        private String content;
        private Double confidence;
    }
}
