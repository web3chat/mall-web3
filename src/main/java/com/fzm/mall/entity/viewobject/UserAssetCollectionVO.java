package com.fzm.mall.entity.viewobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserAssetCollectionVO {
    @Schema(description = "商品编号")
    private String goodsId;
    @Schema(description = "数量")
    private BigDecimal num;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "封面")
    private String cover;

}
