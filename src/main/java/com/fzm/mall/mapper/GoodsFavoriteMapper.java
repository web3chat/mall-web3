package com.fzm.mall.mapper;

import com.fzm.mall.entity.dataobject.GoodsFavoriteDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface GoodsFavoriteMapper {

    int insert(GoodsFavoriteDO favoriteDO);

    int delByGoodsIdAddress(@Param("goodsId") String goodsId, @Param("address") String address);

    GoodsFavoriteDO getByGoodsIdAddress(@Param("goodsId") String goodsId, @Param("address") String address);
}
