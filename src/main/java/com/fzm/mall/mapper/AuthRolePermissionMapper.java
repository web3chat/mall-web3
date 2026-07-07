package com.fzm.mall.mapper;

import com.fzm.mall.entity.dataobject.AuthPermissionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AuthRolePermissionMapper {

    void insertBatch(@Param("childRole") Integer childRole, @Param("permissionIds") List<Integer> permissionIds);

    void deleteByChildRole(@Param("childRole") Integer childRole);

    List<AuthPermissionDO> listByChildRole(@Param("childRole") Integer childRole);
}
