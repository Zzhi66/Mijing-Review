package com.mj.mijing.ai;

import com.mj.mijing.dto.Result;
import com.mj.mijing.service.VoucherOrderService;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * AI 下单工具：支持代金券下单
 */
@Slf4j
@Component
public class VoucherOrderTool {

    @Resource
    private VoucherOrderService voucherOrderService;

    @Tool("为当前登录用户下单抢购秒杀代金券。参数为代金券 ID")
    public String placeOrder(Long voucherId) {
        log.info("AI 尝试代客下单：voucherId={}", voucherId);
        try {
            Result result = voucherOrderService.seckillVoucher(voucherId);
            if (result.getSuccess()) {
                return "下单成功！订单 ID：" + result.getData();
            } else {    
                return "下单失败：" + result.getErrorMsg();
            }
        } catch (Exception e) {
            log.error("AI 下单异常", e);
            return "下单出现异常，请稍后重试。";
        }
    }
}
