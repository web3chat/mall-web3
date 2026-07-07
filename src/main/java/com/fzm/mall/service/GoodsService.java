package com.fzm.mall.service;

import com.fzm.mall.constant.ChainConstant;
import com.fzm.mall.constant.enums.CommonEnum;
import com.fzm.mall.constant.enums.GoodsEnum;
import com.fzm.mall.constant.response.ResponseEnum;
import com.fzm.mall.entity.dataobject.*;
import com.fzm.mall.entity.properties.ContractProperties;
import com.fzm.mall.entity.queryobject.back.MGoodsPageQO;
import com.fzm.mall.mapper.*;
import com.fzm.mall.redis.RedisIdComponent;
import com.fzm.mall.third.chain.contract.ERC1155Manager;
import com.fzm.mall.third.chain.entity.TxResult;
import com.fzm.mall.third.chain.entity.TxResultEnum;
import com.fzm.mall.third.chain.util.Web3jUtils;
import com.fzm.mall.util.AssertUtils;
import com.fzm.mall.util.TimeUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GoodsService {
    private final RedisIdComponent redisIdComponent;
    private final GoodsSpuMapper goodsSpuMapper;
    private final GoodsSkuPropertiesMapper goodsSkuPropertiesMapper;
    private final GoodsSkuMapper goodsSkuMapper;
    private final GoodsMintMapper goodsMintMapper;
    private final GoodsWhiteMapper goodsWhiteMapper;
    private final GoodsFavoriteMapper goodsFavoriteMapper;
    private final ChainContractMapper chainContractMapper;
    private final UserAdminMapper userAdminMapper;
    private final ContractProperties contractProperties;

    @Transactional(rollbackFor = Exception.class)
    public void addOrUpdate(GoodsSpuDO spuDO, GoodsSkuPropertiesDO skuPropDO, List<GoodsSkuDO> skuDOS) {
        GoodsSpuDO selectSpuDO = getSpuByGoodsId(spuDO.getGoodsId());

        BigDecimal minSkuPrice = skuDOS.stream().map(GoodsSkuDO::getPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        spuDO.setPrice(minSkuPrice);
        Integer skuTotal = skuDOS.stream().map(GoodsSkuDO::getTotal).reduce(Integer::sum).orElseThrow();
        spuDO.setTotal(skuTotal);

        // 1、新增
        if (selectSpuDO == null) {
            String goodsId = redisIdComponent.nextSpuId();
            spuDO.setGoodsId(goodsId);
            spuDO.setSales(0);
            spuDO.setFavorite(0);
            spuDO.setStatus(GoodsEnum.SpuStatusEnum.draft.getStatus());
            spuDO.setHiddenStatus(CommonEnum.BoolEnum.NO.getStatus());
            spuDO.setCreateTime(TimeUtils.nowTimestamp());
            spuDO.setRecommend(CommonEnum.BoolEnum.NO.getStatus());
            int ok;
            try {
                ok = goodsSpuMapper.insert(spuDO);
            } catch (Exception e) {
                ok = 0;
            }
            AssertUtils.isTrue(ok == 1, "spu保存失败");

            skuPropDO.setGoodsId(goodsId);
            ok = goodsSkuPropertiesMapper.insert(skuPropDO);
            AssertUtils.isTrue(ok == 1, "skuProp保存失败");

            insertSkus(goodsId, skuDOS, GoodsEnum.MintStatusEnum.mint_wait);

            return;
        }

        //
        AssertUtils.isTrue(selectSpuDO.getAddress().equals(spuDO.getAddress()), "spu.goodsId错误");
        AssertUtils.isTrue(selectSpuDO.getStatus() == GoodsEnum.SpuStatusEnum.draft.getStatus() || selectSpuDO.getStatus() == GoodsEnum.SpuStatusEnum.mint_success.getStatus(), "只有草稿和下架状态可以编辑");

        spuDO.setOriginalStatus(selectSpuDO.getOriginalStatus());

        // 更新SKU Prop
        skuPropDO.setGoodsId(spuDO.getGoodsId());
        int ok = goodsSkuPropertiesMapper.updateByGoodsId(skuPropDO);
        AssertUtils.isTrue(ok == 1, "skuProp更新失败");

        // 2、草稿编辑
        if (selectSpuDO.getStatus() == GoodsEnum.SpuStatusEnum.draft.getStatus()) {
            // 更新SPU
            updateSpuByGoodsId(spuDO);

            // 删除原SKU
            ok = goodsSkuMapper.deleteByGoodsId(spuDO.getGoodsId());
            AssertUtils.isTrue(ok > 0, "清除原SKU失败");

            insertSkus(spuDO.getGoodsId(), skuDOS, GoodsEnum.MintStatusEnum.mint_wait);

            return;
        }

        // 3、已铸造的编辑，盲盒不能由其他类型改过来
        if (spuDO.getType() == GoodsEnum.SpuTypeEnum.blind_box.getType()) {
            AssertUtils.isTrue(selectSpuDO.getType() == GoodsEnum.SpuTypeEnum.blind_box.getType(), "盲盒不能由其他类型商品改过来");
        }

        // 需要新增的sku
        List<GoodsSkuDO> insertSkuDOS = new ArrayList<>();
        // 需要更新的sku
        List<GoodsSkuDO> updateSkuDOS = new ArrayList<>();

        for (GoodsSkuDO skuDO : skuDOS) {
            GoodsSkuDO selectSkuDO = getSkuBySkuId(skuDO.getSkuId());
            // 新增
            if (selectSkuDO == null) {
                insertSkuDOS.add(skuDO);
                continue;
            }
            // 更新
            AssertUtils.isTrue(selectSkuDO.getGoodsId().equals(spuDO.getGoodsId()), "skus.skuId错误");
            AssertUtils.isTrue(skuDO.getTotal() >= selectSkuDO.getTotal(), "skus.total商品的数量必须大于等于原始数量");
            // 增发数量
            skuDO.setOriginalTotal(selectSkuDO.getTotal());
            skuDO.setMintAddNum(skuDO.getTotal() - selectSkuDO.getTotal());

            // 是否需要修改提货类型、是否是盲盒
            if (skuDO.getMintAddNum() == 0) {
                if (!skuDO.getExpressType().equals(selectSkuDO.getExpressType()) || !skuDO.getBlindBoxType().equals(selectSkuDO.getBlindBoxType())) {
                    // 设置TokenPrefix，后续判断时，有就是需要修改提货类型
                    skuDO.setTokenPrefix(selectSkuDO.getTokenPrefix());
                }
            }

            skuDO.setUriUpdate(!spuDO.getCover().equals(selectSpuDO.getCover()) || !spuDO.getDes().equals(selectSpuDO.getDes()) || !skuDO.getTokenName().equals(selectSkuDO.getTokenName()));
            skuDO.setStatus(GoodsEnum.MintStatusEnum.mint_ing.getStatus());
            skuDO.setOriginalStatus(GoodsEnum.MintStatusEnum.mint_success.getStatus());

            updateSkuDOS.add(skuDO);
        }

        UserAdminDO userAdminDO = userAdminMapper.getByAddress(spuDO.getAddress());

        // 商户燃料费
        int newTokenIdCount = 0;
        for (GoodsSkuDO skuDO : insertSkuDOS) {
            newTokenIdCount += skuDO.getTotal();
        }
        for (GoodsSkuDO skuDO : updateSkuDOS) {
            newTokenIdCount += skuDO.getMintAddNum();
        }
        if (newTokenIdCount > 0) {
            long gasLimit = ERC1155Manager.dynamicMintGasLimit(newTokenIdCount);

            TxResult<BigDecimal> tr = Web3jUtils.getBalance(contractProperties.getChainUrl(), "", userAdminDO.getInsideAddress(), contractProperties.getDecimals());
            AssertUtils.isTrue(tr.getResult().compareTo(ChainConstant.btyDiv(gasLimit)) >= 0, ResponseEnum.insufficient_gas);
        }

        // 更新SPU
        spuDO.setStatus(GoodsEnum.SpuStatusEnum.mint_again_ing.getStatus());
        updateSpuByGoodsId(spuDO);

        // 新增SKU
        insertSkus(spuDO.getGoodsId(), insertSkuDOS, GoodsEnum.MintStatusEnum.mint_ing);
        for (GoodsSkuDO skuDO : insertSkuDOS) {
            List<GoodsMintDO> mintDOS = calcMintDOS(spuDO.getGoodsId(), skuDO.getSkuId(), 0, skuDO.getTotal());
            int i = goodsMintMapper.insertBatch(mintDOS);
            AssertUtils.isTrue(i == mintDOS.size(), "保存新增铸造数据失败");
        }

        // 更新SKU
        for (GoodsSkuDO skuDO : updateSkuDOS) {
            // 更新SKU
            updateSkuBySkuId(skuDO);
            // 更新TokenUri
            if (skuDO.getUriUpdate()) {
                int i = goodsMintMapper.updateBatchUriStatusBySkuId(skuDO.getSkuId(), GoodsEnum.MintStatusEnum.mint_success.getStatus(), GoodsEnum.MintStatusEnum.mint_wait.getStatus());
                AssertUtils.isTrue(i > 0, "保存更新铸造数据失败");
            }
            // 增发
            if (skuDO.getMintAddNum() > 0) {
                List<GoodsMintDO> mintDOS = calcMintDOS(spuDO.getGoodsId(), skuDO.getSkuId(), skuDO.getOriginalTotal(), skuDO.getMintAddNum());
                int i = goodsMintMapper.insertBatch(mintDOS);
                AssertUtils.isTrue(i == mintDOS.size(), "保存增发铸造数据失败");
            }
        }

        // 修改元类型
        List<Long> prefixAry = new ArrayList<>();
        List<Boolean> canExpressAry = new ArrayList<>();
        List<Boolean> isBlindBoxAry = new ArrayList<>();

        for (GoodsSkuDO skuDO : updateSkuDOS) {
            if (skuDO.getTokenPrefix() != null) {
                prefixAry.add(skuDO.getTokenPrefix());
                canExpressAry.add(skuDO.getExpressType() == CommonEnum.BoolEnum.YES.getStatus());
                isBlindBoxAry.add(skuDO.getBlindBoxType() == CommonEnum.BoolEnum.YES.getStatus());
            }
        }
        if (CollectionUtils.isEmpty(prefixAry)) {
            return;
        }
        ChainContractDO contractDO = chainContractMapper.getByCtId(ChainConstant.contract_default_contract_id);

        TxResult<String> tr = ERC1155Manager.init(contractProperties.getChainUrl(), contractProperties.getChainId(), contractDO.getAddress())
                .updatePrefixMeta(userAdminDO.getInsidePrivateKey(), prefixAry, canExpressAry, isBlindBoxAry);
        AssertUtils.isTrue(tr.getStatus() == TxResultEnum.SUCCESS, "修改前缀元数据错误：" + tr.getError());
    }

    @Transactional(rollbackFor = Exception.class)
    public void mint(GoodsSpuDO spuDO) {
        String goodsId = spuDO.getGoodsId();
        List<GoodsSkuDO> skuDOS = listSkuByGoodsId(goodsId);

        // 商户燃料费
        int newTokenIdCount = 0;
        for (GoodsSkuDO skuDO : skuDOS) {
            newTokenIdCount += skuDO.getTotal();
        }
        if (newTokenIdCount > 0) {
            long gasLimit = ERC1155Manager.dynamicMintGasLimit(newTokenIdCount);

            UserAdminDO userAdminDO = userAdminMapper.getByAddress(spuDO.getAddress());

            TxResult<BigDecimal> tr = Web3jUtils.getBalance(contractProperties.getChainUrl(), "", userAdminDO.getInsideAddress(), contractProperties.getDecimals());
            AssertUtils.isTrue(tr.getResult().compareTo(ChainConstant.btyDiv(gasLimit)) >= 0, ResponseEnum.insufficient_gas);
        }

        GoodsSpuDO updateSpuDO = new GoodsSpuDO();
        updateSpuDO.setGoodsId(goodsId);
        updateSpuDO.setStatus(GoodsEnum.SpuStatusEnum.mint_ing.getStatus());
        updateSpuDO.setOriginalStatus(GoodsEnum.SpuStatusEnum.draft.getStatus());
        updateSpuByGoodsId(updateSpuDO);

        for (GoodsSkuDO skuDO : skuDOS) {
            GoodsSkuDO updateSkuDO = new GoodsSkuDO();
            updateSkuDO.setSkuId(skuDO.getSkuId());
            updateSkuDO.setStatus(GoodsEnum.MintStatusEnum.mint_ing.getStatus());
            updateSkuDO.setOriginalStatus(GoodsEnum.MintStatusEnum.mint_wait.getStatus());
            updateSkuBySkuId(updateSkuDO);

            List<GoodsMintDO> mintDOS = calcMintDOS(goodsId, skuDO.getSkuId(), 0, skuDO.getTotal());
            int i = goodsMintMapper.insertBatch(mintDOS);
            AssertUtils.isTrue(i == mintDOS.size(), "保存铸造数据失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void retry(GoodsSpuDO spuDO) {
        GoodsSpuDO updateSpuDO = new GoodsSpuDO();
        updateSpuDO.setGoodsId(spuDO.getGoodsId());
        updateSpuDO.setStatus(spuDO.getStatus() == GoodsEnum.SpuStatusEnum.mint_fail.getStatus() ? GoodsEnum.SpuStatusEnum.mint_ing.getStatus() : GoodsEnum.SpuStatusEnum.mint_again_ing.getStatus());
        updateSpuDO.setOriginalStatus(spuDO.getStatus());
        updateSpuByGoodsId(updateSpuDO);

        int i = goodsSkuMapper.updateBatchStatusBySkuId(spuDO.getGoodsId(), GoodsEnum.MintStatusEnum.mint_fail.getStatus(), GoodsEnum.MintStatusEnum.mint_ing.getStatus());
        AssertUtils.isTrue(i > 0, "更新SKU失败");

        int m = goodsMintMapper.updateBatchStatusByGoodsId(spuDO.getGoodsId(), GoodsEnum.MintStatusEnum.mint_fail.getStatus(), GoodsEnum.MintStatusEnum.mint_wait.getStatus());
        int n = goodsMintMapper.updateBatchUriStatusByGoodsId(spuDO.getGoodsId(), GoodsEnum.MintStatusEnum.mint_fail.getStatus(), GoodsEnum.MintStatusEnum.mint_wait.getStatus());
        AssertUtils.isTrue((m + n) > 0, "更新Mint失败");
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteByGoodsId(String goodsId) {
        int ok = goodsSpuMapper.deleteByGoodsId(goodsId);
        AssertUtils.isTrue(ok == 1, "删除SPU失败");
        ok = goodsSkuPropertiesMapper.deleteByGoodsId(goodsId);
        AssertUtils.isTrue(ok == 1, "删除SKU Prop失败");
        ok = goodsSkuMapper.deleteByGoodsId(goodsId);
        AssertUtils.isTrue(ok >= 1, "删除SKU失败");

        goodsWhiteMapper.deleteByGoodsId(goodsId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void addOrUpdateWhite(String goodsId, List<GoodsWhiteDO> whiteDOS) {
        goodsWhiteMapper.deleteByGoodsId(goodsId);

        int i;
        try {
            i = goodsWhiteMapper.insertBatch(whiteDOS);
        } catch (Exception e) {
            i = 0;
        }

        AssertUtils.isTrue(i == whiteDOS.size(), "更新名单失败");
    }

    @Transactional(rollbackFor = Exception.class)
    public void favorite(String goodsId, String address) {
        GoodsSpuDO spuDO = getSpuByGoodsId(goodsId);
        if (spuDO == null) {
            return;
        }

        GoodsFavoriteDO favoriteDO = getFavoriteByGoodsIdAddress(goodsId, address);

        // 未收藏，并且要收藏，则保存信息
        if (favoriteDO == null) {
            favoriteDO = new GoodsFavoriteDO();
            favoriteDO.setGoodsId(goodsId);
            favoriteDO.setAddress(address);
            favoriteDO.setCreateTime(TimeUtils.nowTimestamp());

            int i;
            try {
                i = goodsFavoriteMapper.insert(favoriteDO);
            } catch (Exception e) {
                i = 0;
            }
            AssertUtils.isTrue(i == 1, ResponseEnum.too_many_requests_plz_try_again_later);

            i = goodsSpuMapper.favoriteByGoodsId(goodsId);
            AssertUtils.isTrue(i == 1, ResponseEnum.too_many_requests_plz_try_again_later);
        }
        // 已收藏，并且取消收藏，则删除信息
        else {
            int i = goodsFavoriteMapper.delByGoodsIdAddress(goodsId, address);
            AssertUtils.isTrue(i == 1, ResponseEnum.too_many_requests_plz_try_again_later);

            i = goodsSpuMapper.unFavoriteByGoodsId(goodsId);
            AssertUtils.isTrue(i == 1, ResponseEnum.too_many_requests_plz_try_again_later);
        }
    }

    public void updateSpuByGoodsId(GoodsSpuDO spuDO) {
        int ok = goodsSpuMapper.updateByGoodsId(spuDO);
        AssertUtils.isTrue(ok == 1, "spu更新失败");
    }

    public void updateSkuBySkuId(GoodsSkuDO skuDO) {
        int ok = goodsSkuMapper.updateBySkuId(skuDO);
        AssertUtils.isTrue(ok == 1, "SKU更新失败");
    }

    public GoodsSpuDO getSpuByGoodsId(String goodsId) {
        if (StringUtils.isBlank(goodsId)) {
            return null;
        }
        return goodsSpuMapper.getByGoodsId(goodsId);
    }

    public GoodsSkuPropertiesDO getSkuPropByGoodsId(String goodsId) {
        if (StringUtils.isBlank(goodsId)) {
            return null;
        }
        return goodsSkuPropertiesMapper.getByGoodsId(goodsId);
    }

    public GoodsSkuDO getSkuBySkuId(String skuId) {
        if (StringUtils.isBlank(skuId)) {
            return null;
        }
        return goodsSkuMapper.getBySkuId(skuId);
    }

    public GoodsFavoriteDO getFavoriteByGoodsIdAddress(String goodsId, String address) {
        if (StringUtils.isAnyBlank(goodsId, address)) {
            return null;
        }
        return goodsFavoriteMapper.getByGoodsIdAddress(goodsId, address);
    }

    public List<GoodsSkuDO> listSkuByGoodsId(String goodsId) {
        if (StringUtils.isBlank(goodsId)) {
            return null;
        }
        return goodsSkuMapper.listByGoodsId(goodsId);
    }

    public List<GoodsMintDO> listMintBySkuId(String skuId) {
        return goodsMintMapper.listBySkuId(skuId);
    }

    public List<GoodsWhiteDO> listWhiteByGoodsId(String goodsId) {
        return goodsWhiteMapper.listByGoodsId(goodsId);
    }

    public List<GoodsSpuDO> listSpuByGoodsIds(List<String> goodsIds) {
        if (CollectionUtils.isEmpty(goodsIds)) {
            return Collections.emptyList();
        }
        return goodsSpuMapper.listByGoodsIds(goodsIds);
    }

    public List<GoodsSkuDO> listSkuBySkuIds(List<String> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Collections.emptyList();
        }
        return goodsSkuMapper.listBySkuIds(skuIds);
    }

    public PageInfo<GoodsSpuDO> pageSpu(MGoodsPageQO pageQO) {
        if (pageQO.getOrderType() == null) {
            pageQO.setOrderType(GoodsEnum.OrderTypeEnum.none.getType());
        }
        PageHelper.startPage(pageQO.getPage(), pageQO.getSize());
        List<GoodsSpuDO> dos = goodsSpuMapper.listByPageQO(pageQO);
        return new PageInfo<>(dos);
    }

    private void insertSkus(String goodsId, List<GoodsSkuDO> skuDOS, GoodsEnum.MintStatusEnum mintStatusEnum) {
        if (CollectionUtils.isEmpty(skuDOS)) {
            return;
        }

        for (GoodsSkuDO skuDO : skuDOS) {
            String skuId = redisIdComponent.nextSkuId();
            long tokenPrefix = redisIdComponent.nextTokenPrefix();

            skuDO.setGoodsId(goodsId);
            skuDO.setSkuId(skuId);
            skuDO.setTokenPrefix(tokenPrefix);
            skuDO.setSales(0);
            skuDO.setStatus(mintStatusEnum.getStatus());
            skuDO.setCreateTime(TimeUtils.nowTimestamp());
        }

        int ok;
        try {
            ok = goodsSkuMapper.insertBatch(skuDOS);
        } catch (Exception e) {
            ok = 0;
        }
        AssertUtils.isTrue(ok == skuDOS.size(), "skus保存失败");
    }

    private List<GoodsMintDO> calcMintDOS(String goodsId, String skuId, int defStartNum, int mintNum) {
        int step = (mintNum - 1) / ChainConstant.token_mint_batch_max_num;

        List<GoodsMintDO> mintDOS = new ArrayList<>(Math.max(step, 16));
        for (int i = 0; i <= step; i++) {
            int startNum = defStartNum + (i * ChainConstant.token_mint_batch_max_num) + 1;
            int endNum = defStartNum + ((i != step) ? (i + 1) * ChainConstant.token_mint_batch_max_num : mintNum);

            GoodsMintDO mintDO = new GoodsMintDO();
            mintDO.setGoodsId(goodsId);
            mintDO.setSkuId(skuId);
            mintDO.setStartNum(startNum);
            mintDO.setEndNum(endNum);
            mintDO.setStatus(GoodsEnum.MintStatusEnum.mint_wait.getStatus());
            mintDO.setUriStatus(GoodsEnum.MintStatusEnum.mint_wait.getStatus());
            mintDO.setTxHash("");
            mintDO.setTxNote("");

            mintDOS.add(mintDO);
        }
        return mintDOS;
    }
}
