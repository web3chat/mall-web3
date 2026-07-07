package com.fzm.mall.constant.response;

import lombok.Data;

@Data
public class ResponseVO<T> {
    private int code;
    private String message;
    private T data;

    public ResponseVO(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public ResponseVO(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }


}
