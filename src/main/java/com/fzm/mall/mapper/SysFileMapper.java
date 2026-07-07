package com.fzm.mall.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysFileMapper {

    void insert(@Param("fileUrl") String fileUrl, @Param("createTime") Long createTime);

    String getByFileUrl(@Param("fileUrl") String fileUrl);
}
