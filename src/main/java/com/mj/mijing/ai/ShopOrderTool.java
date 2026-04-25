package com.mj.mijing.ai;

import cn.hutool.core.date.DateUtil;
import com.mj.mijing.entity.ShopAppointment;
import com.mj.mijing.service.ShopAppointmentService;
import com.mj.mijing.utils.RedisIdWorker;
import com.mj.mijing.utils.UserHolder;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * AI 商铺订单/预约工具
 */
@Slf4j
@Component
public class ShopOrderTool {

    @Resource
    private ShopAppointmentService shopAppointmentService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Tool("为当前登录用户预约到店。参数 shopId 是商铺 ID，time 是预约时间（格式：yyyy-MM-dd HH:mm:ss）")
    public String bookShop(Long shopId, String time) {
        log.info("AI 尝试预约到店：shopId={}, time={}", shopId, time);
        try {
            // 1. 获取当前用户 ID
            Long userId = UserHolder.getUser().getId();
            
            // 2. 解析时间
            LocalDateTime appointmentTime;
            try {
                appointmentTime = DateUtil.parseLocalDateTime(time);
            } catch (Exception e) {
                return "预约时间格式错误，请按 yyyy-MM-dd HH:mm:ss 格式提供。";
            }

            // 3. 生成 ID 并保存
            long id = redisIdWorker.nextId("appointment");
            ShopAppointment appointment = new ShopAppointment();
            appointment.setId(id);
            appointment.setUserId(userId);
            appointment.setShopId(shopId);
            appointment.setAppointmentTime(appointmentTime);
            appointment.setCreateTime(LocalDateTime.now());

            boolean success = shopAppointmentService.save(appointment);
            if (success) {
                return String.format("预约成功！预约时间：%s，单号：%d", time, id);
            } else {
                return "预约失败，数据库写入异常。";
            }
        } catch (Exception e) {
            log.error("AI 预约异常", e);
            return "预约处理过程中出现异常，请稍后重试。";
        }
    }
}
