package com.mj.mijing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mj.mijing.entity.Voucher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface VoucherMapper extends BaseMapper<Voucher> {
    /**
     * 查询某商铺所有优惠券（含秒杀信息）
     */
    List<Voucher> queryVoucherOfShop(@Param("shopId") Long shopId);
}
