package com.fzm.mall.entity.queryobject.back;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fzm.mall.entity.queryobject.VaguePageQO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MOrderInfoPageQO extends VaguePageQO {
    @Schema(description = "状态，-1全部，0待支付，1已支付，2已撤销，3已超时", example = "-1")
    private Integer status;

    @Schema(description = "转账状态，-1全部，0等待中，1成功，2失败", example = "-1")
    private Integer txStatus;

    @JsonIgnore
    private Integer deleteStatus;

    @JsonIgnore
    private String sellerAddress;

    @JsonIgnore
    private String buyerAddress;
}
