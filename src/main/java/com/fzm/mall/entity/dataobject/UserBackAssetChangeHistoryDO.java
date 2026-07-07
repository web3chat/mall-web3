package com.fzm.mall.entity.dataobject;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserBackAssetChangeHistoryDO {
    private Long logId;
    private String address;
    private Integer coinType;
    private BigDecimal number;
    private Integer balanceType;
    private Integer logType;
    private Integer withdrawType;

    private Long createTime;
    private String extendData;
}
