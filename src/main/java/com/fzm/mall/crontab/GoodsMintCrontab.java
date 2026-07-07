package com.fzm.mall.crontab;

import com.fzm.mall.constant.ChainConstant;
import com.fzm.mall.constant.enums.CommonEnum;
import com.fzm.mall.constant.enums.GoodsEnum;
import com.fzm.mall.constant.enums.MetaTypeEnum;
import com.fzm.mall.entity.dataobject.*;
import com.fzm.mall.entity.properties.ContractProperties;
import com.fzm.mall.mapper.*;
import com.fzm.mall.redis.RedisCacheExpireEnum;
import com.fzm.mall.redis.RedisLockComponent;
import com.fzm.mall.redis.key.RedisLockKey;
import com.fzm.mall.third.chain.contract.ERC1155Manager;
import com.fzm.mall.third.chain.entity.TokenUri;
import com.fzm.mall.third.chain.entity.TokenUriProperties;
import com.fzm.mall.third.chain.entity.TxResult;
import com.fzm.mall.third.chain.entity.TxResultEnum;
import com.fzm.mall.third.chain.util.TokenUtils;
import com.fzm.mall.third.component.FileComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GoodsMintCrontab {
    private final GoodsSpuMapper goodsSpuMapper;
    private final GoodsSkuMapper goodsSkuMapper;
    private final GoodsMintMapper goodsMintMapper;
    private final UserAdminMapper userAdminMapper;
    private final ChainContractMapper chainContractMapper;
    private final RedisLockComponent redisLockComponent;
    private final FileComponent fileComponent;
    private final ContractProperties contractProperties;

    @Scheduled(cron = "0/5 * * * * ?")
    public void judgeSpuStatus() {
        List<GoodsSpuDO> spuDOS = goodsSpuMapper.listByStatusLimit(List.of(GoodsEnum.SpuStatusEnum.mint_ing.getStatus(), GoodsEnum.SpuStatusEnum.mint_again_ing.getStatus()));
        if (CollectionUtils.isEmpty(spuDOS)) {
            return;
        }

        for (GoodsSpuDO spuDO : spuDOS) {
            String lockKey = String.format(RedisLockKey.Mint.goods_id_judge, spuDO.getGoodsId());
            boolean lock = redisLockComponent.lock(lockKey, lockKey, RedisCacheExpireEnum.expire_minutes_10);
            if (!lock) {
                continue;
            }

            List<GoodsSkuDO> skuDOS = goodsSkuMapper.listByGoodsId(spuDO.getGoodsId());

            // 铸造中
            boolean anyIng = skuDOS.stream().anyMatch(o -> o.getStatus() == GoodsEnum.MintStatusEnum.mint_ing.getStatus());
            if (anyIng) {
                redisLockComponent.unlock(lockKey, lockKey);
                continue;
            }

            boolean allSuccess = skuDOS.stream().allMatch(o -> o.getStatus() == GoodsEnum.MintStatusEnum.mint_success.getStatus());

            GoodsSpuDO updateSpuDO = new GoodsSpuDO();
            updateSpuDO.setGoodsId(spuDO.getGoodsId());
            updateSpuDO.setOriginalStatus(spuDO.getStatus());
            // 铸造成功
            if (allSuccess) {
                updateSpuDO.setStatus(GoodsEnum.SpuStatusEnum.mint_success.getStatus());
            } else {
                // 铸造失败
                updateSpuDO.setStatus(spuDO.getStatus() == GoodsEnum.SpuStatusEnum.mint_ing.getStatus() ? GoodsEnum.SpuStatusEnum.mint_fail.getStatus() : GoodsEnum.SpuStatusEnum.mint_again_fail.getStatus());
            }
            goodsSpuMapper.updateByGoodsId(updateSpuDO);

            redisLockComponent.unlock(lockKey, lockKey);
        }
    }

    @Scheduled(cron = "0/5 * * * * ?")
    public void judgeSkuStatus() {
        List<GoodsSkuDO> skuDOS = goodsSkuMapper.listByStatusLimit(GoodsEnum.MintStatusEnum.mint_ing.getStatus());
        if (CollectionUtils.isEmpty(skuDOS)) {
            return;
        }

        for (GoodsSkuDO skuDO : skuDOS) {
            String lockKey = String.format(RedisLockKey.Mint.sku_id_judge, skuDO.getSkuId());
            boolean lock = redisLockComponent.lock(lockKey, lockKey, RedisCacheExpireEnum.expire_minutes_10);
            if (!lock) {
                continue;
            }

            List<GoodsMintDO> mintDOS = goodsMintMapper.listBySkuId(skuDO.getSkuId());
            // 铸造中
            boolean anyIng = mintDOS.stream().anyMatch(o -> o.getStatus() == GoodsEnum.MintStatusEnum.mint_wait.getStatus() || o.getStatus() == GoodsEnum.MintStatusEnum.mint_ing.getStatus()
                    || o.getUriStatus() == GoodsEnum.MintStatusEnum.mint_wait.getStatus() || o.getUriStatus() == GoodsEnum.MintStatusEnum.mint_ing.getStatus());
            if (anyIng) {
                redisLockComponent.unlock(lockKey, lockKey);
                continue;
            }

            // 铸造成功
            boolean allSuccess = mintDOS.stream().allMatch(o -> o.getStatus() == GoodsEnum.MintStatusEnum.mint_success.getStatus() && o.getUriStatus() == GoodsEnum.MintStatusEnum.mint_success.getStatus());

            GoodsSkuDO updateSkuDO = new GoodsSkuDO();
            updateSkuDO.setSkuId(skuDO.getSkuId());
            updateSkuDO.setOriginalStatus(skuDO.getStatus());
            if (allSuccess) {
                updateSkuDO.setStatus(GoodsEnum.MintStatusEnum.mint_success.getStatus());
            } else {
                // 铸造失败
                updateSkuDO.setStatus(GoodsEnum.MintStatusEnum.mint_fail.getStatus());
            }
            goodsSkuMapper.updateBySkuId(updateSkuDO);

            redisLockComponent.unlock(lockKey, lockKey);
        }
    }

    @Scheduled(cron = "0/5 * * * * ?")
    public void mint() {
        List<GoodsMintDO> mintDOS = goodsMintMapper.listByStatusLimit(GoodsEnum.MintStatusEnum.mint_wait.getStatus());
        if (CollectionUtils.isEmpty(mintDOS)) {
            return;
        }

        ChainContractDO contractDO = chainContractMapper.getByCtId(ChainConstant.contract_default_contract_id);

        for (GoodsMintDO mintDO : mintDOS) {
            GoodsSpuDO spuDO = goodsSpuMapper.getByGoodsId(mintDO.getGoodsId());
            GoodsSkuDO skuDO = goodsSkuMapper.getBySkuId(mintDO.getSkuId());
            UserAdminDO userAdminDO = userAdminMapper.getByAddress(spuDO.getAddress());

            String lockKey = String.format(RedisLockKey.Chain.nonce_of_address, userAdminDO.getInsideAddress());
            boolean lock = redisLockComponent.lock(lockKey, lockKey, RedisCacheExpireEnum.expire_minutes_10);
            if (!lock) {
                continue;
            }

            log.info("商品铸造定时器，执行开始，商品编号：{}，Sku编号：{}，起始序号：{}，结束序号：{}", mintDO.getGoodsId(), mintDO.getSkuId(), mintDO.getStartNum(), mintDO.getEndNum());

            // 更新TokenURI
            if (mintDO.getUriStatus() == GoodsEnum.MintStatusEnum.mint_wait.getStatus()) {
                log.info("商品铸造定时器，更新TokenURI，执行开始");

                // 其他属性
                TokenUriProperties properties = new TokenUriProperties();
                properties.setMetaType(MetaTypeEnum.goods.getType());
                properties.setMetaId(mintDO.getSkuId());

                // uri信息
                TokenUri tokenUri = new TokenUri();
                tokenUri.setImage(skuDO.getCover());
                tokenUri.setDescription(spuDO.getDes());
                tokenUri.setProperties(properties);

                GoodsEnum.MintStatusEnum mintStatusEnum = GoodsEnum.MintStatusEnum.mint_success;

                for (int serial = mintDO.getStartNum(); serial <= mintDO.getEndNum(); serial++) {
                    long tokenId = TokenUtils.tokenId(skuDO.getTokenPrefix(), serial);
                    String tokenIdFullHex = TokenUtils.tokenIdFullHex(tokenId);

                    try {
                        // 默认
                        tokenUri.setName(TokenUtils.tokenName(skuDO.getTokenName(), tokenId));
                        fileComponent.uploadTokenUri(tokenIdFullHex, tokenUri);

                    } catch (Exception e) {
                        log.info("商品铸造定时器，更新TokenUri失败，商品编号：{}，Sku编号：{}，序列号：{}", mintDO.getGoodsId(), mintDO.getSkuId(), serial, e);
                        mintStatusEnum = GoodsEnum.MintStatusEnum.mint_fail;
                        break;
                    }
                }

                GoodsMintDO updateMintDO = new GoodsMintDO();
                updateMintDO.setGoodsId(mintDO.getGoodsId());
                updateMintDO.setSkuId(mintDO.getSkuId());
                updateMintDO.setStartNum(mintDO.getStartNum());
                updateMintDO.setEndNum(mintDO.getEndNum());

                updateMintDO.setUriStatus(mintStatusEnum.getStatus());
                updateMintDO.setOriginalUriStatus(GoodsEnum.MintStatusEnum.mint_wait.getStatus());
                goodsMintMapper.update(updateMintDO);

                log.info("商品铸造定时器，更新TokenURI，执行成功");
            }

            // 铸造
            if (mintDO.getStatus() == GoodsEnum.MintStatusEnum.mint_wait.getStatus()) {
                log.info("商品铸造定时器，批量铸造，执行开始");

                TxResult<String> txResult = ERC1155Manager.init(contractProperties.getChainUrl(), contractProperties.getChainId(), contractDO.getAddress()).mintBatchNFT(userAdminDO.getInsideAddress(), userAdminDO.getInsidePrivateKey(), skuDO.getTokenPrefix(), mintDO.getStartNum(), mintDO.getEndNum(), skuDO.getExpressType() == CommonEnum.BoolEnum.YES.getStatus(), skuDO.getBlindBoxType() == CommonEnum.BoolEnum.YES.getStatus());

                GoodsMintDO updateMintDO = new GoodsMintDO();
                updateMintDO.setGoodsId(mintDO.getGoodsId());
                updateMintDO.setSkuId(mintDO.getSkuId());
                updateMintDO.setStartNum(mintDO.getStartNum());
                updateMintDO.setEndNum(mintDO.getEndNum());
                updateMintDO.setOriginalStatus(GoodsEnum.MintStatusEnum.mint_wait.getStatus());

                if (txResult.getStatus() == TxResultEnum.FAILED) {
                    updateMintDO.setStatus(GoodsEnum.MintStatusEnum.mint_fail.getStatus());
                    updateMintDO.setTxNote(txResult.getError());
                } else {
                    updateMintDO.setStatus(GoodsEnum.MintStatusEnum.mint_success.getStatus());
                    updateMintDO.setTxHash(txResult.getResult());
                    updateMintDO.setTxNote("");
                }
                int i = goodsMintMapper.update(updateMintDO);
                if (i == 0) {
                    log.error("商品铸造定时器，批量铸造，结果保存失败：{}", updateMintDO);
                }

                log.info("商品铸造定时器，批量铸造，执行成功，执行结果：{}", txResult);
            }

            redisLockComponent.unlock(lockKey, lockKey);

            log.info("商品铸造定时器，执行结束，商品编号：{}，Sku编号：{}，起始序号：{}，结束序号：{}", mintDO.getGoodsId(), mintDO.getSkuId(), mintDO.getStartNum(), mintDO.getEndNum());

        }

    }
}
