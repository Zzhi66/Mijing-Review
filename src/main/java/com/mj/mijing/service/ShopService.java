package com.mj.mijing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mj.mijing.dto.Result;
import com.mj.mijing.entity.Shop;

public interface ShopService extends IService<Shop> {
    /** 查询商铺详情（二级缓存 + 缓存击穿/穿透保护） */
    Result queryById(Long id);

    /** 更新商铺（先改DB，再双删缓存） */
    Result update(Shop shop);

    /** 附近商铺（Redis GEO） */
    Result queryShopByType(Integer typeId, Integer current, Double x, Double y);
}
