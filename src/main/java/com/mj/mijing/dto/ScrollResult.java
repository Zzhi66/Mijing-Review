package com.mj.mijing.dto;

import lombok.Data;
import java.util.List;

/**
 * 滚动分页结果（Feed 流）
 */
@Data
public class ScrollResult {
    /** 数据列表 */
    private List<?> list;
    /** 下次查询的最小时间戳 */
    private Long minTime;
    /** 偏移量（同一时间戳内的偏移） */
    private Integer offset;
}
