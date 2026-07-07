package com.fzm.mall.entity.dataobject;

import lombok.Data;

@Data
public class OrderLimitDO {
    private String goodsId;
    private String skuId;
    private String address;
    private Integer num;
}
