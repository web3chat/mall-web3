package com.fzm.mall.mapper;

import com.fzm.mall.entity.dataobject.GoodsWhiteDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface GoodsWhiteMapper {
    int insertBatch(@Param("list") List<GoodsWhiteDO> whiteDOS);

    void deleteByGoodsId(@Param("goodsId") String goodsId);

    List<GoodsWhiteDO> listByGoodsId(@Param("goodsId") String goodsId);

    GoodsWhiteDO getByGoodsIdSkuIdAddress(@Param("goodsId") String goodsId, @Param("skuId") String skuId, @Param("address") String address);
}
