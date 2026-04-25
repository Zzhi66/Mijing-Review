package com.mj.mijing.controller;

import com.mj.mijing.ai.AiChatService;
import com.mj.mijing.dto.Result;
import com.mj.mijing.utils.UserHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * AI 智能客服接口
 */
@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private AiChatService aiChatService;

    /**
     * POST /ai/chat
     * 请求体：{"message": "附近有哪些好的咖啡厅？"}
     */
    @PostMapping("/chat")
    public Result chat(@RequestBody Map<String, String> body) {
        String message = body.get("message");
        if (message == null || message.isBlank()) {
            return Result.fail("消息不能为空");
        }
        Long userId = UserHolder.getUser().getId();
        String reply = aiChatService.chat(userId, message);
        return Result.ok(reply);
    }
}
