package com.fzm.mall.mapper;

import com.fzm.mall.entity.dataobject.GoodsMintDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface GoodsMintMapper {
    int insertBatch(@Param("list") List<GoodsMintDO> mintDOS);

    int updateBatchUriStatusBySkuId(@Param("skuId") String skuId, @Param("fromStatus") int fromStatus, @Param("toStatus") int toStatus);

    int updateBatchStatusByGoodsId(@Param("goodsId") String goodsId, @Param("fromStatus") int fromStatus, @Param("toStatus") int toStatus);

    int updateBatchUriStatusByGoodsId(@Param("goodsId") String goodsId, @Param("fromStatus") int fromStatus, @Param("toStatus") int toStatus);

    int update(GoodsMintDO mintDO);

    List<GoodsMintDO> listBySkuId(@Param("skuId") String skuId);

    List<GoodsMintDO> listByStatusLimit(@Param("status") int status);
}
