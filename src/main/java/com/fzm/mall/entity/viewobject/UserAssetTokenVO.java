package com.fzm.mall.entity.viewobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserAssetTokenVO {
    @Schema(description = "商品编号")
    private String goodsId;
    @Schema(description = "skuId")
    private String skuId;

    @Schema(description = "合约编号")
    private Integer ctId;
    @Schema(description = "tokenId")
    private Long tokenId;

    @Schema(description = "数量")
    private BigDecimal num;

    @Schema(description = "时间")
    private Long updateTime;
}
