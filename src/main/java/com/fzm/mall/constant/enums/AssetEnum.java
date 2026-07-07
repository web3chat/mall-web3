package com.fzm.mall.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class AssetEnum {

    @Getter
    @AllArgsConstructor
    public enum BalanceTypeEnum {
        balance(0, "余额"),
        frozen(1, "冻结"),

        ;
        private final int type;
        private final String message;

    }

    @Getter
    @AllArgsConstructor
    public enum LogTypeEnum {
        withdraw(3, "提现-打币中", CommonEnum.BoolEnum.YES),
        withdraw_chain_confirm(4, "提现-确认中", CommonEnum.BoolEnum.YES),
        withdraw_chain_fail(5, "提现-上链失败", CommonEnum.BoolEnum.YES),
        withdraw_chain_success(6, "提现-上链成功", CommonEnum.BoolEnum.YES),
        order_freeze(7, "订单冻结", CommonEnum.BoolEnum.NO),
        order_unfreeze(8, "订单解冻", CommonEnum.BoolEnum.NO),
        ;
        private final int type;
        private final String message;
        private final CommonEnum.BoolEnum withdrawType;
    }
}
