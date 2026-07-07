package com.fzm.mall.entity.requestobject.back;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class MGoodsSkuPropertiesRO {
    @Schema(description = "标题1")
    private String title1;
    @Schema(description = "值数组1")
    private List<String> value1s;

    @Schema(description = "标题2")
    private String title2;
    @Schema(description = "值数组2")
    private List<String> value2s;
}
