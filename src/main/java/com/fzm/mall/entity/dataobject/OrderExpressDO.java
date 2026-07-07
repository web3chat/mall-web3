package com.fzm.mall.entity.dataobject;

import lombok.Data;

import java.util.List;

@Data
public class OrderExpressDO {
    private String orderId;
    private String sellerAddress;
    private String buyerAddress;
    private String goodsId;
    private String skuId;
    private Integer ctId;
    private String tokenIdJson;
    private Integer num;
    private String txHash;
    private Integer status;
    private String name;
    private String phone;
    private String detail;
    private String note;
    private String expressCode;
    private Long orderTime;
    private Long expressTime;
    private Long confirmTime;
    private Long confirmAutoTime;

    private String refundNote;
    private String refundExpressCode;
    private Long refundApplyTime;
    private Long refundAuditTime;
    private Long refundConfirmTime;
    private Long refundConfirmAutoTime;

    private Integer refundTxStatus;
    private String refundTxHash;
    private String refundTxNote;

    private Integer deleteStatus;

    // ============
    private List<String> orderIds;
    private Integer originalStatus;
    private List<Integer> originalStatusList;
    private Integer originalRefundTxStatus;

}
