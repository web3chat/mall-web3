package com.fzm.mall.mapper;

import com.fzm.mall.entity.dataobject.AirdropWhiteDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AirdropWhiteMapper {

    int insertBatch(@Param("list") List<AirdropWhiteDO> whiteDOS);

    void updateTokenIdJsonById(@Param("id") Long id, @Param("tokenIdJson") String tokenIdJson);

    int deleteByInfoId(@Param("infoId") Long infoId);

    List<AirdropWhiteDO> listByInfoId(@Param("infoId") Long infoId);
}
