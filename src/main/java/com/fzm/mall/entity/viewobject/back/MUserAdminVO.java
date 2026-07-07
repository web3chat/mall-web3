package com.fzm.mall.entity.viewobject.back;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MUserAdminVO {
    @Schema(description = "地址")
    private String address;
    @Schema(description = "所属商户地址")
    private String parentAddress;

    @Schema(description = "状态，0冻结，1正常")
    private Integer status;

    @Schema(description = "角色编号，1管理员，2商户")
    private Integer role;
    @Schema(description = "子角色，0无")
    private Integer childRole;
    //
    @Schema(description = "角色名称")
    private String roleName;
    @Schema(description = "子角色名称")
    private String childRoleName;
    //

    @Schema(description = "昵称")
    private String nickname;
    @Schema(description = "头像")
    private String headUrl;

    @Schema(description = "子账号状态，0初始状态，1商户添加子账号，待同意")
    private Integer applyStatus;

    @Schema(description = "内部地址")
    private String insideAddress;

    @Schema(description = "平台分账比例")
    private BigDecimal profitSharing;

    @Schema(description = "注册IP")
    private String registerIp;
    @Schema(description = "注册时间")
    private Long registerTime;

    @Schema(description = "最近登录IP")
    private String latestLoginIp;
    @Schema(description = "最近登录时间")
    private Long latestLoginTime;
}
