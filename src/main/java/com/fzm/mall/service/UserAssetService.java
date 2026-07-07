package com.fzm.mall.service;

import com.fzm.mall.entity.dataobject.UserAssetCollectionDO;
import com.fzm.mall.entity.dataobject.UserAssetTokenDO;
import com.fzm.mall.entity.queryobject.back.MUserAssetPageQO;
import com.fzm.mall.mapper.UserAssetCollectionMapper;
import com.fzm.mall.mapper.UserAssetTokenMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserAssetService {
    private final UserAssetCollectionMapper userAssetCollectionMapper;
    private final UserAssetTokenMapper userAssetTokenMapper;

    public UserAssetTokenDO getByAddressCtIdTokenId(String address, Integer ctId, Long tokenId) {
        return userAssetTokenMapper.getByAddressCtIdTokenId(address, ctId, tokenId);
    }

    public PageInfo<UserAssetCollectionDO> pageCollection(MUserAssetPageQO pageQO) {
        PageHelper.startPage(pageQO.getPage(), pageQO.getSize());
        List<UserAssetCollectionDO> dos = userAssetCollectionMapper.listByPageQO(pageQO);
        return new PageInfo<>(dos);
    }

    public List<UserAssetTokenDO> listTokenByGoodsIdAddress(String goodsId, String address) {
        return userAssetTokenMapper.listByGoodsIdAddress(goodsId, address);
    }

    public PageInfo<UserAssetTokenDO> page(MUserAssetPageQO pageQO) {
        PageHelper.startPage(pageQO.getPage(), pageQO.getSize());
        List<UserAssetTokenDO> dos = userAssetTokenMapper.listByPageQO(pageQO);
        return new PageInfo<>(dos);
    }
}
