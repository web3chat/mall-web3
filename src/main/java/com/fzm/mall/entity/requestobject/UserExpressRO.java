package com.fzm.mall.entity.requestobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserExpressRO {
    @Schema(description = "编号")
    private Long id;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "地区")
    private String region;
    @Schema(description = "详细地址")
    private String location;
    @Schema(description = "默认地址，0否，1是")
    private Integer status;
}
