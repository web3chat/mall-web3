package com.fzm.mall.third.chain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TxResult<T> {
    private TxResultEnum status;

    private T result;
    private String error;

    public static <T> TxResult<T> success(T result) {
        return new TxResult<>(TxResultEnum.SUCCESS, result, "");
    }

    public static <T> TxResult<T> failure(String error) {
        return new TxResult<>(TxResultEnum.FAILED, null, error);
    }

    public static <T> TxResult<T> failure(String error, T t) {
        return new TxResult<>(TxResultEnum.FAILED, t, error);
    }

    public static <T> TxResult<T> running() {
        return new TxResult<>(TxResultEnum.RUNNING, null, "");
    }
}
