package com.fzm.mall.mapper;

import com.fzm.mall.entity.dataobject.AuthRoleDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AuthRoleMapper {

    void insert(@Param("address") String address, @Param("name") String name);

    void updateByChildRole(@Param("childRole") Integer childRole, @Param("name") String name);

    void deleteByChildRole(@Param("childRole") Integer childRole);

    AuthRoleDO getByChildRole(@Param("childRole") Integer childRole);

    List<AuthRoleDO> listByChildRoles(@Param("list") List<Integer> childRoleList);

    List<AuthRoleDO> listByAddress(@Param("address") String address);
}
