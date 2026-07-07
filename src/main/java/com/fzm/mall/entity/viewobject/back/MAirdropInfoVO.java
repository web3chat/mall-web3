package com.fzm.mall.entity.viewobject.back;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MAirdropInfoVO {
    @Schema(description = "编号")
    private Long infoId;
    @Schema(description = "地址")
    private String address;
    @Schema(description = "名称")
    private String name;

    @Schema(description = "状态，0待上架，1已上架")
    private Integer status;

    @Schema(description = "0等待，1库存检查，2分配token，3成功，4失败")
    private Integer taskStatus;

    @Schema(description = "空投哈希")
    private String txHash;
    @Schema(description = "空投备注")
    private String txNote;

    @Schema(description = "开始空投时间")
    private Long startTime;
    @Schema(description = "创建时间")
    private Long createTime;
}
