package com.fzm.mall.entity.dataobject;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderInfoDO {
    private String orderId;
    private String sellerAddress;
    private String buyerAddress;
    private String goodsId;
    private String skuId;

    private String tokenIdJson;

    private Integer num;
    private BigDecimal price;
    private BigDecimal amount;
    private Integer payChain;
    private Integer payCoin;
    private BigDecimal payPrice;
    private BigDecimal payAmount;
    private String payHash;
    private Integer status;
    private Long orderTime;
    private Long payTime;
    private Long expireTime;
    private Long cancelTime;

    private Integer txStatus;
    private String txHash;
    private String txNote;

    private Integer unfreezeStatus;
    private Long unfreezeTime;

    private String clientIp;
    private String userAgent;

    private Integer deleteStatus;

    private String refundNote;

    private String refundApplyTxHash;

    private BigDecimal refundPrice;
    private BigDecimal refundAmount;

    private Integer refundTxStatus;
    private String refundTxHash;
    private String refundTxNote;

    private Integer expressType;
    private String expressJson;

    // ============
    private List<String> orderIds;
    private Integer originalStatus;
    private Integer originalTxStatus;
    private Integer oldUnfreezeStatus;
    private Integer originalRefundTxStatus;
}
