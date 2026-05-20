package com.mj.mijing.controller;

import com.mj.mijing.dto.Result;
import com.mj.mijing.service.VoucherOrderService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {

    @Resource
    private VoucherOrderService voucherOrderService;

    /**
     * 秒杀下单（高并发入口）
     */
    @PostMapping("/seckill/{id}")
    public Result seckillVoucher(@PathVariable Long id) {
        return voucherOrderService.seckillVoucher(id);
    }

    /**
     * 查询秒杀订单异步创建状态（前端轮询）
     */
    @GetMapping("/status/{orderId}")
    public Result getSeckillOrderStatus(@PathVariable Long orderId) {
        return voucherOrderService.getSeckillOrderStatus(orderId);
    }
}
