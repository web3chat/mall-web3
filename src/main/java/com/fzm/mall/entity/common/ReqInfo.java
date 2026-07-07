package com.fzm.mall.entity.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fzm.mall.constant.enums.LangEnum;
import com.fzm.mall.constant.enums.UserEnum;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class ReqInfo {
    @JsonIgnore
    private String clientIp;
    @JsonIgnore
    private String userAgent;
    @JsonIgnore
    private LangEnum langEnum;

    private Integer uid;

    private String address;
    private String parentAddress;

    private Integer status;

    private Integer role;
    private Integer childRole;

    private String nickname;
    private String headUrl;

    private Integer applyStatus;

    private String registerIp;
    private Long registerTime;

    private String latestLoginIp;
    private Long latestLoginTime;

    public boolean _isTopRole() {
        return StringUtils.isNotBlank(address) && address.equals(parentAddress);
    }

    public boolean _isActiveRole() {
        return role != null && role != UserEnum.RoleEnum.none.getRole() && applyStatus != null && applyStatus == UserEnum.ApplyStatusEnum.normal.getStatus();
    }
}
