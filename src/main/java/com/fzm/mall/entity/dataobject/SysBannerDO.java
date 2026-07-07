package com.fzm.mall.entity.dataobject;

import lombok.Data;

@Data
public class SysBannerDO {
    private Long id;
    private String title;
    private String cover;
    private String target;
    private Integer showOrder;
    private Integer status;
    private Long startTime;
    private Long endTime;
    private Long createTime;
}
