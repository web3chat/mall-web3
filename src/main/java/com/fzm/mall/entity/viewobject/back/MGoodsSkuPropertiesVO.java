package com.fzm.mall.entity.viewobject.back;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MGoodsSkuPropertiesVO {
    @Schema(description = "标题1")
    private String title1;
    @Schema(description = "值数组1")
    @JsonProperty("value1s")
    private String value1Json;

    @Schema(description = "标题2")
    private String title2;
    @Schema(description = "值数组2")
    @JsonProperty("value2s")
    private String value2Json;
}
