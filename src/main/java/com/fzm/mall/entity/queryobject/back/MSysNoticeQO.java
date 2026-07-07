package com.fzm.mall.entity.queryobject.back;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fzm.mall.entity.common.PageQO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MSysNoticeQO extends PageQO {
    @Schema(description = "状态，-1全部，0未上线，1已上线", example = "-1")
    private Integer status;
    @Schema(description = "置顶，-1全部，0否，1是", example = "-1")
    private Integer top;
    @Schema(description = "滚动，-1全部，0否，1是", example = "-1")
    private Integer scroll;

    @JsonIgnore
    private Long nowDateTime;
}
