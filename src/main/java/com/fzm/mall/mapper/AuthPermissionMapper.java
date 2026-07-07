package com.fzm.mall.mapper;

import com.fzm.mall.entity.dataobject.AuthPermissionDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AuthPermissionMapper {
    List<AuthPermissionDO> listAll();
}
