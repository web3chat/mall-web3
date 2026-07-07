package com.fzm.mall.mapper;

import com.fzm.mall.entity.dataobject.GoodsSkuDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GoodsSkuMapper {
    int insertBatch(@Param("list") List<GoodsSkuDO> skuDOS);

    int updateBySkuId(GoodsSkuDO skuDO);

    int updateBatchStatusBySkuId(@Param("goodsId") String goodsId, @Param("fromStatus") int fromStatus, @Param("toStatus") int toStatus);

    int deleteByGoodsId(@Param("goodsId") String goodsId);

    int addSalesBySkuId(@Param("skuId") String skuId, @Param("num") int num);

    int subSalesBySkuId(@Param("skuId") String skuId, @Param("num") int num);

    GoodsSkuDO getBySkuId(@Param("skuId") String skuId);

    List<GoodsSkuDO> listByGoodsId(@Param("goodsId") String goodsId);

    List<GoodsSkuDO> listBySkuIds(@Param("skuIds") List<String> skuIds);

    List<GoodsSkuDO> listByStatusLimit(@Param("status") int status);
}
