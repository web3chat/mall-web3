package com.fzm.mall.component;

import com.fzm.mall.constant.enums.UserEnum;
import com.fzm.mall.constant.response.ResponseEnum;
import com.fzm.mall.entity.common.ThreadInfo;
import com.fzm.mall.entity.dataobject.GoodsSpuDO;
import com.fzm.mall.mapper.GoodsSpuMapper;
import com.fzm.mall.util.AssertUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GoodsComponent {
    private final GoodsSpuMapper goodsSpuMapper;

    public GoodsSpuDO verifyGoodsIdAndGetSpuDO(String goodsId) {
        return verifyGoodsIdAndGetSpuDO(goodsId, null);
    }

    public GoodsSpuDO verifyGoodsIdAndGetSpuDO(String goodsId, Integer role) {
        AssertUtils.isNotBlank(goodsId, ResponseEnum.invalid_goods_id);
        GoodsSpuDO selectSpuDO = goodsSpuMapper.getByGoodsId(goodsId);
        AssertUtils.isNotNull(selectSpuDO, ResponseEnum.invalid_goods_id);
        if (role != null && role == UserEnum.RoleEnum.merchant.getRole()) {
            AssertUtils.isTrue(selectSpuDO.getAddress().equals(ThreadInfo.getInfo().getParentAddress()), ResponseEnum.invalid_goods_id);
        }
        return selectSpuDO;
    }

}
