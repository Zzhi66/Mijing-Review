package com.mj.mijing.ai;

/**
 * LangChain4j AI 助手接口（由 AiServices 动态代理实现）
 */
public interface MijingAiAssistant {
    String chat(String message);
}
