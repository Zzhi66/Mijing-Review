package com.mj.mijing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tb_shop")
public class Shop {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String name;
    private Long typeId;
    private String images;
    private String province;
    private String city;
    private String district;
    private String address;
    private Double x;
    private Double y;
    private Long avgPrice;
    private Integer sold;
    private Integer comments;
    private Integer score;
    private String openHours;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** 距离（非数据库字段，GEO查询时动态赋值） */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private Double distance;
}
