package com.fzm.mall.entity.queryobject.back;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fzm.mall.entity.queryobject.VaguePageQO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MUserAssetPageQO extends VaguePageQO {
    @Schema(description = "SPU类型，-1全部，0普通，1盲盒，2合成", example = "-1")
    private Integer spuType;

    @JsonIgnore
    private String address;
}
