package com.fzm.mall.entity.dataobject;

import com.fzm.mall.entity.common.ReqInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserDO extends ReqInfo {
    private Integer inviteNum;

}
