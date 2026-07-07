package com.fzm.mall.entity.dataobject;

import lombok.Data;

@Data
public class OrderExpressRefundNoteDO {
    private String type;
    private Long time;
    private String note;
    private Integer fromStatus;
    private Integer toStatus;
}
