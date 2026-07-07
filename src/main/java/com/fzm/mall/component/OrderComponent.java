package com.fzm.mall.component;

import com.fzm.mall.constant.enums.CommonEnum;
import com.fzm.mall.constant.response.ResponseEnum;
import com.fzm.mall.entity.dataobject.GoodsSkuDO;
import com.fzm.mall.entity.dataobject.OrderExpressDO;
import com.fzm.mall.entity.viewobject.LogisticsVO;
import com.fzm.mall.mapper.GoodsSkuMapper;
import com.fzm.mall.mapper.GoodsSpuMapper;
import com.fzm.mall.mapper.OrderExpressMapper;
import com.fzm.mall.redis.cache.LogisticsCacheComponent;
import com.fzm.mall.third.component.LogisticsComponent;
import com.fzm.mall.util.AssertUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderComponent {
    private final GoodsSpuMapper goodsSpuMapper;
    private final GoodsSkuMapper goodsSkuMapper;
    private final OrderExpressMapper orderExpressMapper;
    private final LogisticsComponent logisticsComponent;
    private final LogisticsCacheComponent logisticsCacheComponent;

    @Transactional(rollbackFor = Exception.class)
    public GoodsSkuDO openBlindBox(String goodsId) {
        List<GoodsSkuDO> skuDOS = goodsSkuMapper.listByGoodsId(goodsId);
        skuDOS = skuDOS.stream().filter(o -> o.getBlindBoxType() == CommonEnum.BoolEnum.NO.getStatus()).toList();

        GoodsSkuDO rewardSku = null;

        int stockSum = skuDOS.stream().map(GoodsSkuDO::getStock).reduce(Integer::sum).orElse(0);
        int randomInt = RandomUtils.secure().randomInt(0, stockSum);
        int temp = 0;
        for (GoodsSkuDO skuDO : skuDOS) {
            temp += skuDO.getStock();
            if (randomInt < temp) {
                rewardSku = skuDO;
                break;
            }
        }

        AssertUtils.isNotNull(rewardSku, "开启盲盒失败");

        // 增加销量
        int i = goodsSpuMapper.addSalesByGoodsId(rewardSku.getGoodsId(), 1);
        AssertUtils.isTrue(i == 1, "库存不足：" + rewardSku.getGoodsId());

        i = goodsSkuMapper.addSalesBySkuId(rewardSku.getSkuId(), 1);
        AssertUtils.isTrue(i == 1, "库存不足：" + rewardSku.getGoodsId());

        return rewardSku;
    }

    public List<LogisticsVO> getLogisticsVOByOrderId(String orderId, String sellerAddress, String buyerAddress, Integer type) {
        List<LogisticsVO> vos = logisticsCacheComponent.getOrderIdToLogistics(orderId, type);
        if (CollectionUtils.isNotEmpty(vos)) {
            return vos;
        }

        OrderExpressDO expressDO = orderExpressMapper.getByOrderId(orderId);
        AssertUtils.isNotNull(expressDO, ResponseEnum.invalid_parameter);
        if (StringUtils.isNotBlank(sellerAddress)) {
            AssertUtils.isTrue(expressDO.getSellerAddress().equals(sellerAddress), ResponseEnum.invalid_parameter);
        }
        if (StringUtils.isNotBlank(buyerAddress)) {
            AssertUtils.isTrue(expressDO.getBuyerAddress().equals(buyerAddress), ResponseEnum.invalid_parameter);
        }

        if (type == 0) {
            vos = logisticsComponent.getLogistics(expressDO.getExpressCode(), expressDO.getPhone());
        } else {
            vos = logisticsComponent.getLogistics(expressDO.getRefundExpressCode(), expressDO.getPhone());
        }

        logisticsCacheComponent.setOrderIdToLogistics(orderId, type, vos);

        return vos;
    }
}
