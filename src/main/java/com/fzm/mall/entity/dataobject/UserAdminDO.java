package com.fzm.mall.entity.dataobject;

import com.fzm.mall.entity.common.ReqInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserAdminDO extends ReqInfo {
    private String insideAddress;
    private String insidePrivateKey;

    private BigDecimal profitSharing;

}
