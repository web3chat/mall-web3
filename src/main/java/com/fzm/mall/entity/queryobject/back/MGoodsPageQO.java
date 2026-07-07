package com.fzm.mall.entity.queryobject.back;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fzm.mall.entity.queryobject.VaguePageQO;
import com.fzm.mall.util.TimeUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MGoodsPageQO extends VaguePageQO {
    @Schema(description = "状态，-1全部，0草稿，1发行中，2发行失败，3发行成功待上架，4已上架，5修改中，6增发失败", example = "-1")
    private Integer status;

    @Schema(description = "隐藏状态，-1全部，0否，1是", example = "-1")
    private Integer hiddenStatus;

    @Schema(description = "排序方式，0综合排序，1销量正序，2销量倒序，3价格正序，4价格倒序，5销售时间正序，6销售时间倒序", example = "-1")
    private Integer orderType;

    @Schema(description = "推荐状态，-1全部，0否，1是", example = "-1")
    private Integer recommend;

    @Schema(description = "菜单编号")
    private Integer menuId;

    @JsonIgnore
    private String address;

    @JsonIgnore
    private Long nowTimestamp = TimeUtils.nowTimestamp();
}
