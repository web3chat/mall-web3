package com.fzm.mall.entity.dataobject;

import lombok.Data;

@Data
public class UserAdminLogDO {
    private Long id;
    private String address;
    private String parentAddress;

    private String lang;
    private Long duration;
    private String method;
    private String uri;

    private String paramsJson;
    private String resultJson;

    private String clientIp;
    private String userAgent;

    private Long createTime;

    // ============
    private String uriName;
}
