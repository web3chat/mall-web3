package com.fzm.mall.entity.requestobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class OrderRO {
    @Schema(description = "skuId")
    private String skuId;
    @Schema(description = "数量")
    private Integer num;

    @Schema(description = "是否直接提货，0否，1是")
    private Integer expressType;

    private ExpressDetail expressDetail;

    @Data
    public static class ExpressDetail {
        private String name;
        private String phone;
        private String detail;
        private String note;
    }
}
