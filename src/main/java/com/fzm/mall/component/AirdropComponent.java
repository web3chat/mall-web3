package com.fzm.mall.component;

import com.alibaba.fastjson2.JSON;
import com.fzm.mall.constant.ChainConstant;
import com.fzm.mall.constant.enums.MetaTypeEnum;
import com.fzm.mall.entity.dataobject.*;
import com.fzm.mall.entity.properties.ContractProperties;
import com.fzm.mall.mapper.AirdropWhiteMapper;
import com.fzm.mall.mapper.ChainContractMapper;
import com.fzm.mall.mapper.GoodsSkuMapper;
import com.fzm.mall.mapper.GoodsSpuMapper;
import com.fzm.mall.redis.RedisIdComponent;
import com.fzm.mall.third.chain.contract.ERC1155Manager;
import com.fzm.mall.third.chain.contract.FzmERC1155;
import com.fzm.mall.third.chain.entity.TxResult;
import com.fzm.mall.util.AssertUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AirdropComponent {
    private final AirdropWhiteMapper airdropWhiteMapper;
    private final GoodsSpuMapper goodsSpuMapper;
    private final GoodsSkuMapper goodsSkuMapper;
    private final ChainContractMapper chainContractMapper;

    private final RedisIdComponent redisIdComponent;
    private final ContractProperties contractProperties;

    @Transactional(rollbackFor = Exception.class)
    public void checkStock(AirdropInfoDO infoDO) {
        List<AirdropWhiteDO> whiteDOS = airdropWhiteMapper.listByInfoId(infoDO.getInfoId());
        for (AirdropWhiteDO whiteDO : whiteDOS) {
            if (whiteDO.getMetaType() == MetaTypeEnum.goods.getType()) {
                GoodsSkuDO skuDO = goodsSkuMapper.getBySkuId(whiteDO.getMetaId());

                // 增加销量
                int i = goodsSpuMapper.addSalesByGoodsId(skuDO.getGoodsId(), whiteDO.getNum());
                AssertUtils.isTrue(i == 1, "库存不足：" + whiteDO.getMetaId());

                i = goodsSkuMapper.addSalesBySkuId(skuDO.getSkuId(), whiteDO.getNum());
                AssertUtils.isTrue(i == 1, "库存不足：" + whiteDO.getMetaId());
            }
        }
    }

    public void assignToken(AirdropInfoDO infoDO) {
        List<AirdropWhiteDO> whiteDOS = airdropWhiteMapper.listByInfoId(infoDO.getInfoId());
        for (AirdropWhiteDO whiteDO : whiteDOS) {
            // 已经分配过
            if (StringUtils.isNotBlank(whiteDO.getTokenIdJson()) && CollectionUtils.isNotEmpty(JSON.parseArray(whiteDO.getTokenIdJson()))) {
                continue;
            }

            if (whiteDO.getMetaType() == MetaTypeEnum.goods.getType()) {
                GoodsSkuDO skuDO = goodsSkuMapper.getBySkuId(whiteDO.getMetaId());

                List<Long> tokenIds = redisIdComponent.getTokenIds(skuDO.getSkuId(), skuDO.getTokenPrefix(), whiteDO.getNum());
                airdropWhiteMapper.updateTokenIdJsonById(whiteDO.getId(), JSON.toJSONString(tokenIds));
            }
        }
    }

    public TxResult<String> airdrop(AirdropInfoDO infoDO, UserAdminDO userAdminDO) {
        ChainContractDO contractDO = chainContractMapper.getByCtId(ChainConstant.contract_default_contract_id);

        List<FzmERC1155.MultiTX> multiTxs = new ArrayList<>();
        List<AirdropWhiteDO> whiteDOS = airdropWhiteMapper.listByInfoId(infoDO.getInfoId());
        for (AirdropWhiteDO whiteDO : whiteDOS) {
            List<BigInteger> ids = JSON.parseArray(whiteDO.getTokenIdJson(), Long.class).stream().map(BigInteger::valueOf).toList();
            multiTxs.add(new FzmERC1155.MultiTX(whiteDO.getAddress(), ids));
        }
        //
        return ERC1155Manager.init(contractProperties.getChainUrl(), contractProperties.getChainId(), contractDO.getAddress()).muxBatchTransferNFT(userAdminDO.getInsidePrivateKey(), multiTxs);
    }
}
