package com.fzm.mall.mapper;

import com.fzm.mall.entity.dataobject.SysMenuDO;
import com.fzm.mall.entity.queryobject.back.MSysMenuQO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysMenuMapper {

    void insert(SysMenuDO menuDO);

    void updateById(SysMenuDO menuDO);

    void delById(@Param("menuId") Integer menuId);

    SysMenuDO getById(@Param("menuId") Integer menuId);

    List<SysMenuDO> listByPageQO(MSysMenuQO pageQO);
}
