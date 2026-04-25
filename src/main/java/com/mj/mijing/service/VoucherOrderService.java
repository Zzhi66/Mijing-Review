package com.mj.mijing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mj.mijing.dto.Result;
import com.mj.mijing.entity.VoucherOrder;

public interface VoucherOrderService extends IService<VoucherOrder> {
    /**
     * 秒杀下单：Lua 脚本校验资格 → 写入 Kafka → 异步创建订单
     */
    Result seckillVoucher(Long voucherId);

    /**
     * 异步创建订单（由 Kafka 消费者调用）
     */
    void createVoucherOrder(VoucherOrder voucherOrder);
}
