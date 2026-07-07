package com.fzm.mall.mapper;

import com.fzm.mall.entity.dataobject.OrderBlindBoxDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderBlindBoxMapper {
    int insert(OrderBlindBoxDO boxDO);

    int updateByOrderId(OrderBlindBoxDO boxDO);

    OrderBlindBoxDO getByTxHash(@Param("txHash") String txHash);

    List<OrderBlindBoxDO> listByTxStatus(@Param("txStatus") Integer txStatus);

}
