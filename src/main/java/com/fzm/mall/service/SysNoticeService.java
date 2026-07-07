package com.fzm.mall.service;

import com.fzm.mall.constant.enums.CommonEnum;
import com.fzm.mall.entity.dataobject.SysNoticeDO;
import com.fzm.mall.entity.queryobject.back.MSysNoticeQO;
import com.fzm.mall.mapper.SysNoticeMapper;
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
public class SysNoticeService {
    private final SysNoticeMapper sysNoticeMapper;

    public void addOrUpdate(SysNoticeDO noticeDO) {
        SysNoticeDO selectDO = getById(noticeDO.getId());
        if (selectDO == null) {
            noticeDO.setStatus(CommonEnum.BoolEnum.NO.getStatus());
            noticeDO.setCreateTime(TimeUtils.nowTimestamp());
            sysNoticeMapper.insert(noticeDO);
        } else {
            AssertUtils.isTrue(selectDO.getStatus() == CommonEnum.BoolEnum.NO.getStatus(), "只有下架状态的可以编辑");
            sysNoticeMapper.updateById(noticeDO);
        }
    }

    public void updateById(SysNoticeDO noticeDO) {
        sysNoticeMapper.updateById(noticeDO);
    }

    public void deleteById(Long id) {
        sysNoticeMapper.delById(id);
    }

    public SysNoticeDO getById(Long id) {
        if (id == null) {
            return null;
        }
        return sysNoticeMapper.getById(id);
    }


    public PageInfo<SysNoticeDO> pageNotice(MSysNoticeQO pageQO) {
        PageHelper.startPage(pageQO.getPage(), pageQO.getSize());
        List<SysNoticeDO> dos = sysNoticeMapper.listByPageQO(pageQO);
        return new PageInfo<>(dos);
    }
}
