package com.fzm.mall.entity.requestobject.back;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class MGoodsSpuRO {
    @Schema(description = "编号")
    private String goodsId;

    @Schema(description = "类型，0普通，1盲盒，2合成")
    private Integer type;

    @Schema(description = "菜单编号")
    private List<Integer> menuIds;
    @Schema(description = "类目")
    private List<Integer> classifies;

    @Schema(description = "名称，最多32个字符")
    private String name;
    @Schema(description = "描述，可为空")
    private String des;
    @Schema(description = "详情")
    private String detail;

    @Schema(description = "封面")
    private String cover;
    @Schema(description = "图片")
    private List<String> images;

    @Schema(description = "限购数量，-1表示不限购，0表示不可购买")
    private Integer orderLimit;

    @Schema(description = "销售方式，0常规销售，1白名单销售")
    private Integer saleType;
    @Schema(description = "开始售卖时间")
    private Long saleTime;
    @Schema(description = "非常规销售转为常规销售的开始时间")
    private Long saleTimeNormal;

}
