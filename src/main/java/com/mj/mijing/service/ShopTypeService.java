package com.mj.mijing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mj.mijing.dto.Result;
import com.mj.mijing.entity.ShopType;

public interface ShopTypeService extends IService<ShopType> {
    /** 查询商铺类型列表（Redis + Caffeine 缓存） */
    Result queryTypeList();
}
