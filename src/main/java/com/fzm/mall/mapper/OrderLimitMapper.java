package com.fzm.mall.mapper;

import com.fzm.mall.entity.dataobject.OrderLimitDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderLimitMapper {

    int insert(OrderLimitDO limitDO);

    int addNum(@Param("goodsId") String goodsId, @Param("skuId") String skuId, @Param("address") String address, @Param("num") int num, @Param("limit") int limit);

    int subNum(@Param("goodsId") String goodsId, @Param("skuId") String skuId, @Param("address") String address, @Param("num") int num);

    OrderLimitDO getByGoodsIdAddress(@Param("goodsId") String goodsId, @Param("skuId") String skuId, @Param("address") String address);
}
