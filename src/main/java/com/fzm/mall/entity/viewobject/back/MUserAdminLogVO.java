package com.fzm.mall.entity.viewobject.back;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MUserAdminLogVO {
    @Schema(description = "地址")
    private String address;
    @Schema(description = "语言")
    private String lang;
    @Schema(description = "用时，毫秒")
    private Long duration;

    @Schema(description = "输入参数")
    private String paramsJson;
    @Schema(description = "输出参数")
    private String resultJson;

    @Schema(description = "ip")
    private String clientIp;
    @Schema(description = "用户代理")
    private String userAgent;

    @Schema(description = "时间")
    private Long createTime;

    // ============
    @Schema(description = "路径")
    private String uriName;
}
