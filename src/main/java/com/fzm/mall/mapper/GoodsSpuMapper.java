package com.fzm.mall.mapper;

import com.fzm.mall.entity.dataobject.GoodsSpuDO;
import com.fzm.mall.entity.queryobject.back.MGoodsPageQO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GoodsSpuMapper {
    int insert(GoodsSpuDO spuDO);

    int updateByGoodsId(GoodsSpuDO spuDO);

    void updateStatusByAddress(@Param("address") String address, @Param("fromStatus") int fromStatus, @Param("toStatus") int toStatus);

    int deleteByGoodsId(@Param("goodsId") String goodsId);

    int addSalesByGoodsId(@Param("goodsId") String goodsId, @Param("num") int num);

    int subSalesByGoodsId(@Param("goodsId") String goodsId, @Param("num") int num);

    int favoriteByGoodsId(@Param("goodsId") String goodsId);

    int unFavoriteByGoodsId(@Param("goodsId") String goodsId);

    GoodsSpuDO getByGoodsId(@Param("goodsId") String goodsId);

    List<GoodsSpuDO> listByGoodsIds(@Param("goodsIds") List<String> goodsIds);

    List<GoodsSpuDO> listByStatusLimit(@Param("list") List<Integer> status);

    List<GoodsSpuDO> listByPageQO(MGoodsPageQO pageQO);
}
