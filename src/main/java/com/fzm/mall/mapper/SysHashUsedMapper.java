package com.fzm.mall.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysHashUsedMapper {
    int insert(@Param("txHash") String txHash, @Param("createTime") Long createTime);

    String getByTxHash(@Param("txHash") String txHash);
}
