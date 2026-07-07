package com.fzm.mall.mapper;

import com.fzm.mall.entity.dataobject.OrderInfoDO;
import com.fzm.mall.entity.queryobject.back.MOrderInfoPageQO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderInfoMapper {
    int insert(OrderInfoDO infoDO);

    int updateByOrderId(OrderInfoDO infoDO);

    OrderInfoDO getByOrderId(@Param("orderId") String orderId);

    List<OrderInfoDO> listExpired(@Param("dateTime") Long dateTime, @Param("status") Integer status);

    List<OrderInfoDO> listTransfer(@Param("status") Integer status, @Param("txStatus") Integer txStatus);

    List<OrderInfoDO> listRefundTransfer(@Param("status") Integer status, @Param("txStatus") Integer txStatus);

    List<OrderInfoDO> listByPageQO(MOrderInfoPageQO pageQO);

    List<OrderInfoDO> listUnfreeze(@Param("unfreezeStatus") Integer unfreezeStatus, @Param("nowTimestamp") Long nowTimestamp);
}
