package com.fzm.mall.util;

import com.fzm.mall.entity.dataobject.GoodsSkuDO;
import com.fzm.mall.entity.dataobject.UserAdminDO;
import com.fzm.mall.entity.viewobject.MerchantVO;
import com.fzm.mall.entity.viewobject.TokenMetaVO;
import com.fzm.mall.third.chain.util.TokenUtils;

public class FormatVOUtils {

    public static TokenMetaVO getTokenMetaVO(GoodsSkuDO skuDO, Long tokenId) {
        TokenMetaVO metaVO = getTokenMetaVO(skuDO);
        metaVO.setTokenId(tokenId);
        metaVO.setName(TokenUtils.tokenName(metaVO.getName(), tokenId));
        metaVO.setTokenName(TokenUtils.tokenName(metaVO.getTokenName(), tokenId));

        return metaVO;
    }

    public static TokenMetaVO getTokenMetaVO(GoodsSkuDO skuDO) {
        return BeanCopierUtils.copy(skuDO, TokenMetaVO.class);
    }

    public static MerchantVO getMerchantVO(UserAdminDO userAdminDO) {
        MerchantVO merchantVO = new MerchantVO();
        merchantVO.setAddress(userAdminDO.getAddress());
        merchantVO.setName(userAdminDO.getNickname());
        merchantVO.setHeadUrl(userAdminDO.getHeadUrl());
        merchantVO.setInsideAddress(userAdminDO.getInsideAddress());

        return merchantVO;
    }
}
