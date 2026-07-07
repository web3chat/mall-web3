package com.fzm.mall.entity.viewobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class LogisticsVO {
    @Schema(description = "快递名称")
    private String name;
    @Schema(description = "快递单号")
    private String code;

    @Schema(description = "物流状态")
    private String statusDesc;

    private List<Detail> details;

    @Data
    public static class Detail {
        @Schema(description = "时间")
        private Long time;
        @Schema(description = "信息")
        private String desc;
    }
}
