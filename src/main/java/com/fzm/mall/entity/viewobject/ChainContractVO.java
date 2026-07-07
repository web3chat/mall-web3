package com.fzm.mall.entity.viewobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ChainContractVO {
    @Schema(description = "合约编号")
    private Integer ctId;
    @Schema(description = "合约类型")
    private String type;
    @Schema(description = "合约名称")
    private String name;
    @Schema(description = "合约地址")
    private String address;
}
