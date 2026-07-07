package com.fzm.mall.mapper;

import com.fzm.mall.entity.dataobject.OrderExpressDO;
import com.fzm.mall.entity.queryobject.back.MOrderExpressPageQO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderExpressMapper {
    int insert(OrderExpressDO expressDO);

    int updateByOrderId(OrderExpressDO expressDO);

    OrderExpressDO getByOrderId(@Param("orderId") String orderId);

    List<OrderExpressDO> listAutoConfirm(@Param("dateTime") Long dateTime, @Param("status") Integer status);

    List<OrderExpressDO> listRefundAutoConfirm(@Param("dateTime") Long dateTime, @Param("status") Integer status);

    List<OrderExpressDO> listTransfer(@Param("status") Integer status, @Param("txStatus") Integer txStatus);

    List<OrderExpressDO> listByPageQO(MOrderExpressPageQO pageQO);
}
