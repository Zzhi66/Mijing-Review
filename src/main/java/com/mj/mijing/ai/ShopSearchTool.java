package com.mj.mijing.ai;

import com.mj.mijing.dto.Result;
import com.mj.mijing.entity.Shop;
import com.mj.mijing.service.ShopService;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI Function Calling 工具：商铺查询
 * LangChain4j 会自动将此工具注册到 AI 模型
 */
@Slf4j
@Component
public class ShopSearchTool {

    @Resource
    private ShopService shopService;

    @Tool("根据关键词搜索商铺，返回商铺列表信息")
    public String searchShop(String keyword) {
        log.info("AI调用商铺查询工具：keyword={}", keyword);
        List<Shop> shops = shopService.query()
                .like("name", keyword)
                .last("LIMIT 5")
                .list();
        if (shops.isEmpty()) {
            return "未找到相关商铺：" + keyword;
        }
        return shops.stream()
                .map(s -> String.format("【%s】地址：%s，均价：%d元，评分：%d/5，营业时间：%s",
                        s.getName(), s.getAddress(),
                        s.getAvgPrice() == null ? 0 : s.getAvgPrice(),
                        s.getScore() == null ? 0 : s.getScore(),
                        s.getOpenHours() == null ? "未知" : s.getOpenHours()))
                .collect(Collectors.joining("\n"));
    }
}
