package com.fzm.mall.entity.dataobject;

import lombok.Data;

@Data
public class UserExpressDO {
    private Long id;
    private String address;
    private String name;
    private String phone;
    private String region;
    private String location;
    private Integer status;
    private Long createTime;
}
