package com.fzm.mall.mapper;

import com.fzm.mall.entity.dataobject.SysBannerDO;
import com.fzm.mall.entity.queryobject.back.MSysBannerQO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysBannerMapper {

    void insert(SysBannerDO bannerDO);

    void updateById(SysBannerDO bannerDO);

    void delById(@Param("id") Long id);

    SysBannerDO getById(@Param("id") Long id);

    List<SysBannerDO> listByPageQO(MSysBannerQO pageQO);
}
