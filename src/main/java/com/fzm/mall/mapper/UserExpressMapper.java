package com.fzm.mall.mapper;

import com.fzm.mall.entity.dataobject.UserExpressDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserExpressMapper {

    void insert(UserExpressDO expressDO);

    void update(UserExpressDO expressDO);

    void updateStatusByAddress(@Param("address") String address, @Param("toStatus") Integer toStatus);

    void deleteById(@Param("id") Long id);

    UserExpressDO getById(@Param("id") Long id);

    List<UserExpressDO> listByAddress(@Param("address") String address);
}
