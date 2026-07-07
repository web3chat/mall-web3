package com.fzm.mall.entity.dataobject;

import lombok.Data;

@Data
public class GoodsMintDO {
    private String goodsId;
    private String skuId;
    private Integer startNum;
    private Integer endNum;
    private Integer status;
    private Integer uriStatus;
    private String txHash;
    private String txNote;

    // ============
    private Integer originalStatus;
    private Integer originalUriStatus;
}
