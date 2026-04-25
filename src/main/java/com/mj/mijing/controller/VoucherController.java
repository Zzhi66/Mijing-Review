package com.mj.mijing.controller;

import com.mj.mijing.dto.Result;
import com.mj.mijing.entity.Voucher;
import com.mj.mijing.service.VoucherService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/voucher")
public class VoucherController {

    @Resource
    private VoucherService voucherService;

    /** 查询商铺优惠券 */
    @GetMapping("/list/{shopId}")
    public Result queryVoucherOfShop(@PathVariable Long shopId) {
        return voucherService.queryVoucherOfShop(shopId);
    }

    /** 添加普通优惠券 */
    @PostMapping
    public Result addVoucher(@RequestBody Voucher voucher) {
        voucherService.save(voucher);
        return Result.ok(voucher.getId());
    }

    /** 添加秒杀券（含库存、开始/结束时间） */
    @PostMapping("/seckill")
    public Result addSeckillVoucher(@RequestBody Voucher voucher) {
        voucherService.addSeckillVoucher(voucher);
        return Result.ok(voucher.getId());
    }
}
