package com.fzm.mall.mapper;

import com.fzm.mall.entity.dataobject.UserAdminDO;
import com.fzm.mall.entity.queryobject.back.MUserAdminPageQO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserAdminMapper {
    int register(UserAdminDO userAdminDO);

    int updateInsideByAddress(UserAdminDO userAdminDO);

    int updateByAddress(UserAdminDO userAdminDO);

    UserAdminDO getByAddress(@Param("address") String address);

    UserAdminDO getByInsideAddress(@Param("insideAddress") String insideAddress);

    List<UserAdminDO> listByAddressList(@Param("list") List<String> addressList);

    List<UserAdminDO> listByPageQO(MUserAdminPageQO pageQO);
}
