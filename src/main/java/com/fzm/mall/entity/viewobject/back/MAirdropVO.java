package com.fzm.mall.entity.viewobject.back;

import lombok.Data;

import java.util.List;

@Data
public class MAirdropVO {
    private MAirdropInfoVO info;
    private List<MAirdropWhiteVO> whites;
}
