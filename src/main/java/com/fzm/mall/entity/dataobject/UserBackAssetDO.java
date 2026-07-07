package com.fzm.mall.entity.dataobject;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserBackAssetDO {
    private String address;
    private Integer coinType;
    private BigDecimal balance;
    private BigDecimal frozenBalance;
}
