package com.fzm.mall.mapper;

import com.fzm.mall.entity.dataobject.GoodsSkuPropertiesDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GoodsSkuPropertiesMapper {
    int insert(GoodsSkuPropertiesDO skuPropDO);

    int updateByGoodsId(GoodsSkuPropertiesDO skuPropDO);

    int deleteByGoodsId(@Param("goodsId") String goodsId);

    GoodsSkuPropertiesDO getByGoodsId(@Param("goodsId") String goodsId);
}
