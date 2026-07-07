package com.fzm.mall.entity.requestobject.back;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MOrderExpressRefundAuditRO {
    @Schema(description = "订单编号")
    private String orderId;

    @Schema(description = "0不同意，1同意")
    private Integer status;

    @Schema(description = "是否需要用户发送物流：0不需要，1需要")
    private Integer needExpress;

    @Schema(description = "备注")
    private String note;
}
