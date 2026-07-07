package com.fzm.mall.mapper;

import com.fzm.mall.entity.dataobject.UserAdminLogDO;
import com.fzm.mall.entity.queryobject.back.MUserAdminLogPageQO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserAdminLogMapper {
    int insert(UserAdminLogDO logDO);

    List<UserAdminLogDO> listByPageQO(MUserAdminLogPageQO pageQO);
}
