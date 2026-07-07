package com.fzm.mall.entity.dataobject;

import lombok.Data;

import java.util.List;

@Data
public class OrderBlindBoxDO {
    private String orderId;

    private String sellerAddress;
    private String buyerAddress;

    private String goodsId;
    private String skuId;

    private Integer ctId;
    private Long tokenId;
    private String txHash;

    private String rewardSkuId;
    private Long rewardTokenId;
    private String rewardTxHash;
    private String rewardTxNote;

    private Integer rewardTxStatus;

    private Long orderTime;

    // ============
    private List<String> orderIds;
    private Integer originalTxStatus;
}
