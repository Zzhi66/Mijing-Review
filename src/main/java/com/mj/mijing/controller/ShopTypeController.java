package com.mj.mijing.controller;

import com.mj.mijing.dto.Result;
import com.mj.mijing.service.ShopTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {

    @Resource
    private ShopTypeService shopTypeService;

    /** 商铺类型列表 */
    @GetMapping("/list")
    public Result queryTypeList() {
        return shopTypeService.queryTypeList();
    }
}
