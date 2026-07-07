package com.fzm.mall.service;

import com.fzm.mall.constant.enums.CommonEnum;
import com.fzm.mall.entity.dataobject.SysMenuDO;
import com.fzm.mall.entity.queryobject.back.MSysMenuQO;
import com.fzm.mall.mapper.SysMenuMapper;
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
public class SysMenuService {
    private final SysMenuMapper sysMenuMapper;

    public void addOrUpdate(SysMenuDO menuDO) {
        SysMenuDO selectDO = getById(menuDO.getMenuId());
        if (selectDO == null) {
            menuDO.setStatus(CommonEnum.BoolEnum.NO.getStatus());
            menuDO.setCreateTime(TimeUtils.nowTimestamp());
            sysMenuMapper.insert(menuDO);
        } else {
            AssertUtils.isTrue(selectDO.getStatus() == CommonEnum.BoolEnum.NO.getStatus(), "只有下架状态的可以编辑");
            sysMenuMapper.updateById(menuDO);
        }
    }

    public void updateById(SysMenuDO menuDO) {
        sysMenuMapper.updateById(menuDO);
    }

    public void deleteById(Integer menuId) {
        sysMenuMapper.delById(menuId);
    }


    public SysMenuDO getById(Integer menuId) {
        if (menuId == null) {
            return null;
        }
        return sysMenuMapper.getById(menuId);
    }

    public PageInfo<SysMenuDO> pageMenu(MSysMenuQO pageQO) {
        PageHelper.startPage(pageQO.getPage(), pageQO.getSize());
        List<SysMenuDO> dos = sysMenuMapper.listByPageQO(pageQO);
        return new PageInfo<>(dos);
    }

    public List<SysMenuDO> listMenu(MSysMenuQO pageQO) {
        return sysMenuMapper.listByPageQO(pageQO);
    }
}
