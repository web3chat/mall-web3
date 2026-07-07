package com.fzm.mall.entity.queryobject.back;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fzm.mall.entity.common.PageQO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MSysBannerQO extends PageQO {
    @Schema(description = "状态，-1全部，0未上线，1已上线", example = "-1")
    private Integer status;


    @JsonIgnore
    private Long nowDateTime;
}
