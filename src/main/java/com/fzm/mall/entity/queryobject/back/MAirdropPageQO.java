package com.fzm.mall.entity.queryobject.back;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fzm.mall.entity.queryobject.VaguePageQO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MAirdropPageQO extends VaguePageQO {
    @Schema(description = "上架状态，-1全部，0下架，1上架", example = "-1")
    private Integer status;

    @Schema(description = "空投状态，-1全部，0等待，1库存检查，2分配token，3成功，4失败", example = "-1")
    private Integer taskStatus;

    @JsonIgnore
    private String address;
}
