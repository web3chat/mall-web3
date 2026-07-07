package com.fzm.mall.entity.dataobject;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GoodsSpuDO {
    private String goodsId;
    private String address;

    private Integer type;

    private String menuJson;
    private String classifyJson;

    private String name;
    private String des;
    private String detail;

    private String cover;
    private String imageJson;

    private BigDecimal price;

    private Integer total;
    private Integer sales;
    private Integer stock;
    private Integer favorite;

    private Integer orderLimit;

    private Integer saleType;
    private Long saleTime;
    private Long saleTimeNormal;

    private Integer status;
    private Integer hiddenStatus;

    private Long createTime;

    private Integer recommend;
    private Integer showOrder;
    // ============
    private Integer originalStatus;

}
