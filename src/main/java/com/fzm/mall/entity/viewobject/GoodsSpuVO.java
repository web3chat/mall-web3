package com.fzm.mall.entity.viewobject;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GoodsSpuVO {
    @Schema(description = "编号")
    private String goodsId;

    @Schema(description = "类型，0普通，1盲盒，2合成")
    private Integer type;

    @Schema(description = "类目")
    @JsonProperty("classifies")
    private String classifyJson;

    @Schema(description = "名称，最多32个字符")
    private String name;
    @Schema(description = "描述，可为空")
    private String des;
    @Schema(description = "详情")
    private String detail;

    @Schema(description = "封面")
    private String cover;
    @Schema(description = "图片")
    @JsonProperty("images")
    private String imageJson;

    @Schema(description = "最低的价格")
    private BigDecimal price;

    @Schema(description = "总量")
    private Integer total;
    @Schema(description = "总销量")
    private Integer sales;
    @Schema(description = "库存")
    private Integer stock;
    @Schema(description = "总收藏")
    private Integer favorite;

    @Schema(description = "限购数量，-1表示不限购，0表示不可购买")
    private Integer orderLimit;

    @Schema(description = "销售方式，0常规销售，1白名单销售")
    private Integer saleType;
    @Schema(description = "开始售卖时间")
    private Long saleTime;
    @Schema(description = "非常规销售转为常规销售的开始时间")
    private Long saleTimeNormal;
}
