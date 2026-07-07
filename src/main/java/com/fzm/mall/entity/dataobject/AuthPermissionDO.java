package com.fzm.mall.entity.dataobject;

import lombok.Data;

@Data
public class AuthPermissionDO {
    private Integer id;
    private Integer parentId;
    private String name;
    private Integer role;
    private Integer level;
    private String uri;
}
