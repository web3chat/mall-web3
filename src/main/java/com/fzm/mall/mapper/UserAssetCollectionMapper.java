package com.fzm.mall.mapper;

import com.fzm.mall.entity.dataobject.UserAssetCollectionDO;
import com.fzm.mall.entity.queryobject.back.MUserAssetPageQO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserAssetCollectionMapper {

    void insert(@Param("goodsId") String goodsId, @Param("address") String address, @Param("time") Long time);

    void updateNum(@Param("goodsId") String goodsId, @Param("address") String address, @Param("time") Long time);

    UserAssetCollectionDO getByGoodsIdAddress(@Param("goodsId") String goodsId, @Param("address") String address);

    List<UserAssetCollectionDO> listByPageQO(MUserAssetPageQO pageQO);
}
