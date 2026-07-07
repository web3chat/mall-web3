package com.fzm.mall.third.chain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IndexEnum {
    front(0),
    back(1),
    ;
    private final int index;
}
