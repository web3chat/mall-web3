package com.fzm.mall.entity.dataobject;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserAssetTokenDO {
    private String goodsId;
    private String skuId;
    private String address;
    private Integer ctId;
    private Long tokenId;
    private BigDecimal num;

    private Long updateTime;
}
