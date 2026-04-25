package com.mj.mijing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mj.mijing.entity.SeckillVoucher;

public interface SeckillVoucherService extends IService<SeckillVoucher> {

    /**
     * 扣减库存（乐观锁：stock > 0 才扣减）
     *
     * @param voucherId 优惠券ID
     * @return 是否扣减成功
     */
    boolean deductStock(Long voucherId);

    /**
     * 回滚库存（订单取消/退款时恢复）
     *
     * @param voucherId 优惠券ID
     */
    void rollbackStock(Long voucherId);

    /**
     * 判断秒杀是否在有效时间窗口内
     *
     * @param voucherId 优惠券ID
     * @return 是否在有效时间内
     */
    boolean isWithinSeckillTime(Long voucherId);
}
