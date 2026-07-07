package com.fzm.mall.entity.viewobject.back;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MGoodsMintVO {
    @Schema(description = "起始序列号")
    private Integer startNum;
    @Schema(description = "起始序列号")
    private Integer endNum;

    @Schema(description = "铸造状态，0等待发行，1发行中，2发行失败，3发行成功")
    private Integer status;
    @Schema(description = "uri状态，0等待更新，1更新中，2更新失败，3更新成功")
    private Integer uriStatus;

    @Schema(description = "哈希")
    private String txHash;

    @Schema(description = "备注")
    private String txNote;
}
