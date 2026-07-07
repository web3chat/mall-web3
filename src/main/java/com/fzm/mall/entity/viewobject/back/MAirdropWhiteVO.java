package com.fzm.mall.entity.viewobject.back;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MAirdropWhiteVO {
    @Schema(description = "地址")
    private String address;
    @Schema(description = "物品类型，0商品")
    private Integer metaType;
    @Schema(description = "物品编号")
    private String metaId;
    @Schema(description = "数量")
    private Integer num;

    @Schema(description = "空投的tokenIds")
    @JsonProperty("tokenIds")
    private String tokenIdJson;

    @Schema(description = "物品名称")
    private String metaName;
}
