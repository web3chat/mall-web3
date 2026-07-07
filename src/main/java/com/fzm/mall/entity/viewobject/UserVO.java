package com.fzm.mall.entity.viewobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserVO {
    @Schema(description = "uid")
    private Integer uid;

    @Schema(description = "地址")
    private String address;
    @Schema(description = "上级地址")
    private String parentAddress;

    @Schema(description = "状态，0冻结，1正常")
    private Integer status;

    @Schema(description = "昵称")
    private String nickname;
    @Schema(description = "头像")
    private String headUrl;

    @Schema(description = "邀请的用户数量")
    private Integer inviteNum;

    @Schema(description = "注册时间")
    private Long registerTime;

    @Schema(description = "最近登录时间")
    private Long latestLoginTime;
}
