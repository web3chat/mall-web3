package com.fzm.mall.mapper;

import com.fzm.mall.entity.dataobject.UserDO;
import com.fzm.mall.entity.queryobject.back.MUserPageQO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    int register(UserDO userDO);

    int updateByAddress(UserDO userDO);

    int addInviteNumByAddress(@Param("address") String address, @Param("num") int num);

    UserDO getByUid(@Param("uid") long uid);

    UserDO getByAddress(@Param("address") String address);

    List<UserDO> listByPageQO(MUserPageQO pageQO);
}
