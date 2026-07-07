package com.fzm.mall.entity.viewobject.back;

import com.fzm.mall.entity.viewobject.OrderInfoVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MOrderInfoVO extends OrderInfoVO {
    @Schema(description = "打币备注")
    private String txNote;

    @Schema(description = "下单IP")
    private String clientIp;
}
