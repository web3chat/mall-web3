package com.fzm.mall.entity.dataobject;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GoodsSkuDO {
    private String goodsId;
    private String skuId;
    private String address;

    private Long tokenPrefix;

    private String propValue1;
    private String propValue2;

    private String name;
    private String tokenName;

    private String cover;

    private BigDecimal price;

    private Integer total;
    private Integer sales;
    private Integer stock;

    private Integer orderLimit;
    private Integer orderPack;

    private Integer expressType;
    private Integer blindBoxType;

    private String traceHash;

    private Integer status;

    private Long createTime;

    // ============
    private Integer originalTotal;
    private Integer mintAddNum;

    private Boolean uriUpdate;

    private Integer originalStatus;

}
