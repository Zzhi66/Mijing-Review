package com.mj.mijing.controller;

import com.mj.mijing.dto.Result;
import com.mj.mijing.entity.Shop;
import com.mj.mijing.service.ShopService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/shop")
public class ShopController {

    @Resource
    private ShopService shopService;

    /** 查询商铺详情（二级缓存） */
    @GetMapping("/{id}")
    public Result queryShopById(@PathVariable Long id) {
        return shopService.queryById(id);
    }

    /** 更新商铺（先改DB，再双删缓存） */
    @PutMapping
    public Result updateShop(@RequestBody Shop shop) {
        return shopService.update(shop);
    }

    /** 附近商铺（GEO 空间检索） */
    @GetMapping("/geo/list")
    public Result queryShopByGeo(
            @RequestParam Integer typeId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(required = false) Double x,
            @RequestParam(required = false) Double y) {
        return shopService.queryShopByType(typeId, current, x, y);
    }
}
