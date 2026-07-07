package com.fzm.mall.entity.dataobject;

import lombok.Data;

@Data
public class SysMenuDO {
    private Integer menuId;

    private String title;

    private String cover;

    private Integer status;
    private Integer showOrder;

    private Long createTime;
}
