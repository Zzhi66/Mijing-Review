package com.mj.mijing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tb_blog")
public class Blog {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long shopId;
    private Long userId;
    private String title;
    private String images;
    private String content;
    private Integer liked;
    private Integer comments;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** 非数据库字段：当前用户是否点赞 */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private Boolean isLike;
    /** 非数据库字段：发布者信息 */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String name;
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String icon;
}
