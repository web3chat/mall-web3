package com.fzm.mall.entity.queryobject.back;

import com.fzm.mall.entity.queryobject.VaguePageQO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MUserBackAssetLogPageQO extends VaguePageQO {
    @Schema(description = """
            记录类型，
            -1全部，
            0提现申请，
            1提现申请撤销，
            2提现申请审核未通过，
            3提现申请审核通过打币中，
            4提现上链确认中，
            5提现上链失败，
            6提现上链成功，
            7订单冻结，
            8订单解冻
            """, example = "-1")
    private Integer logType;

    @Schema(description = "提币记录，-1全部，0否，1是", example = "-1")
    private Integer withdrawType;

    @Schema(description = "币类型，-1全部，0BTY，1USD", example = "-1")
    private Integer coinType;

    @Schema(description = "开始时间")
    private Long startTime;
    @Schema(description = "结束时间")
    private Long endTime;

    @Schema(description = "商家地址，仅平台可用")
    private String address;
}
