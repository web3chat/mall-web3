package com.fzm.mall.entity.viewobject.back;

import com.fzm.mall.entity.viewobject.MerchantVO;
import com.fzm.mall.entity.viewobject.OrderExpressVO;
import com.fzm.mall.entity.viewobject.TokenMetaVO;
import lombok.Data;

@Data
public class MOrderVO {
    private MOrderInfoVO info;

    private OrderExpressVO express;

    private TokenMetaVO meta;

    private MerchantVO merchant;
}
