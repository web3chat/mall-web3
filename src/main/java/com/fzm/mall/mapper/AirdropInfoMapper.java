package com.fzm.mall.mapper;

import com.fzm.mall.entity.dataobject.AirdropInfoDO;
import com.fzm.mall.entity.queryobject.back.MAirdropPageQO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AirdropInfoMapper {

    int insert(AirdropInfoDO infoDO);

    int updateByInfoId(AirdropInfoDO infoDO);

    int deleteByInfoId(@Param("infoId") Long infoId);

    AirdropInfoDO getByInfoId(@Param("infoId") Long infoId);

    List<AirdropInfoDO> listByStatus(@Param("status") Integer status, @Param("taskStatus") Integer taskStatus, @Param("nowDateTime") Long nowDateTime);

    List<AirdropInfoDO> listByPageQO(MAirdropPageQO pageQO);
}
