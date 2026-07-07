package com.fzm.mall.mapper;

import com.fzm.mall.entity.dataobject.ChainContractDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChainContractMapper {

    int insert(@Param("ctId") Integer ctId, @Param("type") String type, @Param("name") String name, @Param("address") String address);

    ChainContractDO getByCtId(@Param("ctId") Integer ctId);

    List<ChainContractDO> list();
}
