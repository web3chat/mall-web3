package com.fzm.mall.entity.dataobject;

import lombok.Data;

@Data
public class SysNoticeDO {
    private Long id;

    private String classifyJson;

    private String title;
    private String content;

    private String cover;

    private Integer status;
    private Integer top;
    private Integer scroll;

    private Long activeTime;
    private Long createTime;

}
