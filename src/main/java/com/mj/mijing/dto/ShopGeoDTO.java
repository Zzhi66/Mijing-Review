package com.mj.mijing.dto;

import lombok.Data;

/**
 * 商铺 GEO 信息 DTO
 */
@Data
public class ShopGeoDTO {
    private Long id;
    private String name;
    /** 距离（公里） */
    private Double distance;
}
