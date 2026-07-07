package com.fzm.mall.service;

import com.fzm.mall.constant.enums.AirdropEnum;
import com.fzm.mall.constant.enums.CommonEnum;
import com.fzm.mall.constant.response.ResponseEnum;
import com.fzm.mall.entity.dataobject.AirdropInfoDO;
import com.fzm.mall.entity.dataobject.AirdropWhiteDO;
import com.fzm.mall.entity.queryobject.back.MAirdropPageQO;
import com.fzm.mall.mapper.AirdropInfoMapper;
import com.fzm.mall.mapper.AirdropWhiteMapper;
import com.fzm.mall.util.AssertUtils;
import com.fzm.mall.util.TimeUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AirdropService {
    private final AirdropInfoMapper airdropInfoMapper;
    private final AirdropWhiteMapper airdropWhiteMapper;

    @Transactional(rollbackFor = Exception.class)
    public void addOrUpdate(AirdropInfoDO infoDO, List<AirdropWhiteDO> whiteDOS) {
        AirdropInfoDO selectDO = getInfoByInfoId(infoDO.getInfoId());
        // 新增
        if (selectDO == null) {
            infoDO.setStatus(CommonEnum.BoolEnum.NO.getStatus());
            infoDO.setTaskStatus(AirdropEnum.TaskStatusEnum.wait.getStatus());
            infoDO.setTxHash("");
            infoDO.setTxNote("");
            infoDO.setCreateTime(TimeUtils.nowTimestamp());
            int i = airdropInfoMapper.insert(infoDO);
            AssertUtils.isTrue(i == 1, ResponseEnum.too_many_requests_plz_try_again_later);
        }
        // 编辑
        else {
            AssertUtils.isTrue(selectDO.getAddress().equals(infoDO.getAddress()), "infoId错误");
            AssertUtils.isTrue(selectDO.getStatus() == CommonEnum.BoolEnum.NO.getStatus(), "只有下架状态的可以编辑");
            AssertUtils.isTrue(selectDO.getTaskStatus() == AirdropEnum.TaskStatusEnum.wait.getStatus(), "只有未空投的可以编辑");

            infoDO.setOriginalStatus(CommonEnum.BoolEnum.NO.getStatus());
            infoDO.setOriginalTaskStatus(AirdropEnum.TaskStatusEnum.wait.getStatus());

            int i = airdropInfoMapper.updateByInfoId(infoDO);
            AssertUtils.isTrue(i == 1, ResponseEnum.too_many_requests_plz_try_again_later);
        }

        // 更新名单
        if (CollectionUtils.isNotEmpty(whiteDOS)) {
            whiteDOS.forEach(o -> o.setInfoId(infoDO.getInfoId()));

            airdropWhiteMapper.deleteByInfoId(infoDO.getInfoId());
            airdropWhiteMapper.insertBatch(whiteDOS);
        }

    }

    public void updateInfoByInfoId(AirdropInfoDO infoDO) {
        int i = airdropInfoMapper.updateByInfoId(infoDO);
        AssertUtils.isTrue(i == 1, ResponseEnum.too_many_requests_plz_try_again_later);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteByInfoId(Long infoId) {
        int i = airdropInfoMapper.deleteByInfoId(infoId);
        AssertUtils.isTrue(i == 1, "删除失败");
        airdropWhiteMapper.deleteByInfoId(infoId);
    }

    public AirdropInfoDO getInfoByInfoId(Long infoId) {
        if (infoId == null) {
            return null;
        }
        return airdropInfoMapper.getByInfoId(infoId);
    }

    public List<AirdropWhiteDO> listWhiteByInfoId(Long infoId) {
        return airdropWhiteMapper.listByInfoId(infoId);
    }

    public PageInfo<AirdropInfoDO> pageInfo(MAirdropPageQO pageQO) {
        PageHelper.startPage(pageQO.getPage(), pageQO.getSize());
        List<AirdropInfoDO> dos = airdropInfoMapper.listByPageQO(pageQO);
        return new PageInfo<>(dos);
    }
}
