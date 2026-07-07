package com.fzm.mall.entity.dataobject;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserAssetCollectionDO {
    private String goodsId;
    private String address;
    private BigDecimal num;

}
