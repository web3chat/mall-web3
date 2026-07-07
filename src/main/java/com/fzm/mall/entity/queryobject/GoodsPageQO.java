package com.fzm.mall.entity.queryobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GoodsPageQO extends VaguePageQO {

    @Schema(description = "商家地址")
    private String address;

    @Schema(description = "菜单编号")
    private Integer menuId;

    @Schema(description = "排序方式，0综合排序，1销量正序，2销量倒序，3价格正序，4价格倒序，5销售时间正序，6销售时间倒序", example = "-1")
    private Integer orderType;
}
