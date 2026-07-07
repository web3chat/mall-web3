package com.fzm.mall.service;

import com.fzm.mall.constant.enums.CommonEnum;
import com.fzm.mall.entity.dataobject.SysBannerDO;
import com.fzm.mall.entity.queryobject.back.MSysBannerQO;
import com.fzm.mall.mapper.SysBannerMapper;
import com.fzm.mall.util.AssertUtils;
import com.fzm.mall.util.TimeUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SysBannerService {
    private final SysBannerMapper sysBannerMapper;

    public void addOrUpdate(SysBannerDO bannerDO) {
        SysBannerDO selectDO = getById(bannerDO.getId());
        if (selectDO == null) {
            bannerDO.setStatus(CommonEnum.BoolEnum.NO.getStatus());
            bannerDO.setCreateTime(TimeUtils.nowTimestamp());
            sysBannerMapper.insert(bannerDO);
        } else {
            AssertUtils.isTrue(selectDO.getStatus() == CommonEnum.BoolEnum.NO.getStatus(), "只有下架状态的可以编辑");
            sysBannerMapper.updateById(bannerDO);
        }
    }

    public void updateById(SysBannerDO bannerDO) {
        sysBannerMapper.updateById(bannerDO);
    }

    public void deleteById(Long id) {
        sysBannerMapper.delById(id);
    }

    public SysBannerDO getById(Long id) {
        if (id == null) {
            return null;
        }
        return sysBannerMapper.getById(id);
    }

    public List<SysBannerDO> listByStatus(CommonEnum.BoolEnum boolEnum) {
        MSysBannerQO pageQO = new MSysBannerQO();
        pageQO.setStatus(boolEnum.getStatus());
        pageQO.setNowDateTime(TimeUtils.nowTimestamp());
        return sysBannerMapper.listByPageQO(pageQO);
    }

    public PageInfo<SysBannerDO> pageBanner(MSysBannerQO pageQO) {
        PageHelper.startPage(pageQO.getPage(), pageQO.getSize());
        List<SysBannerDO> dos = sysBannerMapper.listByPageQO(pageQO);
        return new PageInfo<>(dos);
    }
}
