package com.fzm.mall.entity.viewobject.back;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MUserBackAssetVO {
    @Schema(description = "地址")
    private String address;
    @Schema(description = "币类型，0BTY，1USD")
    private Integer coinType;
    @Schema(description = "余额")
    private BigDecimal balance;
    @Schema(description = "冻结中的余额")
    private BigDecimal frozenBalance;
    @Schema(description = "链上余额")
    private BigDecimal chainBalance;
}
