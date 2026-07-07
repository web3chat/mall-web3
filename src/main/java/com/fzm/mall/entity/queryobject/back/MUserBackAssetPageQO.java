package com.fzm.mall.entity.queryobject.back;

import com.fzm.mall.entity.common.PageQO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MUserBackAssetPageQO extends PageQO {

    @Schema(description = "币类型，-1全部，0BTY，1USD", example = "-1")
    private Integer coinType;

    @Schema(description = "商家地址，仅平台可用")
    private String address;
}
