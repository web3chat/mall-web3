package com.fzm.mall.entity.viewobject.back;

import com.fzm.mall.entity.viewobject.MerchantVO;
import lombok.Data;

import java.util.List;

@Data
public class MGoodsVO {
    private MGoodsSpuVO spu;
    private MGoodsSkuPropertiesVO skuProp;
    private List<MGoodsSkuVO> skus;

    private MerchantVO merchant;
}
