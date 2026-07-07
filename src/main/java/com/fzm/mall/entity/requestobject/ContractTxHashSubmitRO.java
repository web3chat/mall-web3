package com.fzm.mall.entity.requestobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ContractTxHashSubmitRO {

    @Schema(description = "合约编号")
    private Integer ctId;

    @Schema(description = "交易哈希")
    private String txHash;

    @Schema(description = "类型：1开启盲盒，2合成")
    private Integer respType;
}
