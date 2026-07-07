package com.fzm.mall.mapper;

import com.fzm.mall.entity.dataobject.SysNoticeDO;
import com.fzm.mall.entity.queryobject.back.MSysNoticeQO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysNoticeMapper {

    void insert(SysNoticeDO noticeDO);

    void updateById(SysNoticeDO noticeDO);

    void delById(@Param("id") Long id);

    SysNoticeDO getById(@Param("id") Long id);

    List<SysNoticeDO> listByPageQO(MSysNoticeQO pageQO);
}
