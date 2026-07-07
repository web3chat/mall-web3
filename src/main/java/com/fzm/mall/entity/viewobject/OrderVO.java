package com.fzm.mall.entity.viewobject;

import lombok.Data;

@Data
public class OrderVO {
    private OrderInfoVO info;

    private OrderExpressVO express;

    private TokenMetaVO meta;

    private MerchantVO merchant;
}
