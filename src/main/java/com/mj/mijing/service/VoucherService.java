package com.mj.mijing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mj.mijing.dto.Result;
import com.mj.mijing.entity.Voucher;

public interface VoucherService extends IService<Voucher> {
    /** 查询商铺优惠券列表 */
    Result queryVoucherOfShop(Long shopId);

    /** 添加秒杀券（含秒杀信息，写入数据库+预热Redis库存）*/
    void addSeckillVoucher(Voucher voucher);
}
