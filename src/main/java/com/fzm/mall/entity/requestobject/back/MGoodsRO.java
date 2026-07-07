package com.fzm.mall.entity.requestobject.back;

import lombok.Data;

import java.util.List;

@Data
public class MGoodsRO {
    private MGoodsSpuRO spu;
    private MGoodsSkuPropertiesRO skuProp;
    private List<MGoodsSkuRO> skus;
}
