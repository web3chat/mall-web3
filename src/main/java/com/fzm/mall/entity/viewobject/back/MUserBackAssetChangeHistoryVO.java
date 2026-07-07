package com.fzm.mall.entity.viewobject.back;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MUserBackAssetChangeHistoryVO {
    @Schema(description = "记录编号")
    private Long logId;
    @Schema(description = "地址")
    private String address;
    @Schema(description = "币类型，0BTY，1USD")
    private Integer coinType;
    @Schema(description = "数量")
    private BigDecimal number;
    @Schema(description = "类型，0可用余额，1冻结中")
    private Integer balanceType;
    @Schema(description = """
            记录类型，
            0提现申请，
            1提现申请撤销，
            2提现申请审核未通过，
            3提现申请审核通过打币中，
            4提现上链确认中，
            5提现上链失败，
            6提现上链成功，
            7订单冻结，
            8订单解冻
            """)
    private Integer logType;

    @Schema(description = "时间")
    private Long createTime;
    @Schema(description = "拓展信息")
    private String extendData;
}
