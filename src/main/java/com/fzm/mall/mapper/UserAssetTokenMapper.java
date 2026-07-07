package com.fzm.mall.mapper;

import com.fzm.mall.entity.dataobject.UserAssetTokenDO;
import com.fzm.mall.entity.queryobject.back.MUserAssetPageQO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface UserAssetTokenMapper {
    int insert(@Param("goodsId") String goodsId, @Param("skuId") String skuId, @Param("address") String address, @Param("ctId") Integer ctId, @Param("tokenId") Long tokenId, @Param("num") BigDecimal num, @Param("time") Long time);

    int updateNum(@Param("goodsId") String goodsId, @Param("skuId") String skuId, @Param("address") String address, @Param("ctId") Integer ctId, @Param("tokenId") Long tokenId, @Param("num") BigDecimal num, @Param("time") Long time);

    UserAssetTokenDO getByAddressCtIdTokenId(@Param("address") String address, @Param("ctId") Integer ctId, @Param("tokenId") Long tokenId);

    List<UserAssetTokenDO> listByGoodsIdAddress(@Param("goodsId") String goodsId, @Param("address") String address);

    List<UserAssetTokenDO> listByPageQO(MUserAssetPageQO pageQO);
}
