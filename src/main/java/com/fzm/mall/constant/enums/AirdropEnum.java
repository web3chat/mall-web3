package com.fzm.mall.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class AirdropEnum {
    @Getter
    @AllArgsConstructor
    public enum TaskStatusEnum {
        wait(0, "等待"),
        check_stock(1, "库存检查"),
        assign_token(2, "分配token"),
        success(3, "成功"),
        fail(4, "失败"),
        ;
        private final int status;
        private final String message;
    }
}
