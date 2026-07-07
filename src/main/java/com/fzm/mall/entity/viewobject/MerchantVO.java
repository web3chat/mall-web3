package com.fzm.mall.entity.viewobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MerchantVO {
    @Schema(description = "商户地址")
    private String address;
    @Schema(description = "店铺名称")
    private String name;
    @Schema(description = "店铺头像")
    private String headUrl;

    @Schema(description = "商户内部地址")
    private String insideAddress;
}
