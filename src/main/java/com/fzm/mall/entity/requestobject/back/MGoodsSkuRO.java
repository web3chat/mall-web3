package com.fzm.mall.entity.requestobject.back;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MGoodsSkuRO {
    @Schema(description = "skuId")
    private String skuId;

    @Schema(description = "参数1")
    private String propValue1;
    @Schema(description = "参数2")
    private String propValue2;

    @Schema(description = "名称，最多32个字符")
    private String name;
    @Schema(description = "token名称，最多32个字符")
    private String tokenName;

    @Schema(description = "封面")
    private String cover;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "总量")
    private Integer total;

    @Schema(description = "限购数量，-1表示不限购，0表示不可购买")
    private Integer orderLimit;
    @Schema(description = "购买数量必须是该值的整数倍")
    private Integer orderPack;

    @Schema(description = "提货类型，0不可提货，1可以提货")
    private Integer expressType;
    @Schema(description = "是否盲盒，0不是，1是")
    private Integer blindBoxType;

    @Schema(description = "溯源哈希，可以为空")
    private String traceHash;

}
