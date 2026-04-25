package com.mj.mijing.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 门店预约到店实体
 */
@Data
@TableName("tb_shop_appointment")
public class ShopAppointment {
    @TableId
    private Long id;
    private Long userId;
    private Long shopId;
    private LocalDateTime appointmentTime;
    private LocalDateTime createTime;
}
