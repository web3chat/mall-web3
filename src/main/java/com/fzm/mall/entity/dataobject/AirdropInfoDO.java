package com.fzm.mall.entity.dataobject;

import lombok.Data;

@Data
public class AirdropInfoDO {
    private Long infoId;
    private String address;
    private String name;
    private Integer status;
    private Integer taskStatus;
    private String txHash;
    private String txNote;
    private Long startTime;
    private Long createTime;

    // ============
    private Integer originalStatus;
    private Integer originalTaskStatus;
}
