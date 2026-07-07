package com.fzm.mall.component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.fzm.mall.constant.enums.*;
import com.fzm.mall.entity.dataobject.*;
import com.fzm.mall.entity.properties.ContractProperties;
import com.fzm.mall.entity.queryobject.back.MUserAdminPageQO;
import com.fzm.mall.mapper.*;
import com.fzm.mall.redis.RedisIdComponent;
import com.fzm.mall.redis.cache.UserCacheComponent;
import com.fzm.mall.service.UserAdminService;
import com.fzm.mall.third.chain.contract.ERC1155Manager;
import com.fzm.mall.third.chain.contract.FzmERC1155;
import com.fzm.mall.third.chain.entity.*;
import com.fzm.mall.third.chain.wallet.ETHUtils;
import com.fzm.mall.util.AesUtils;
import com.fzm.mall.util.HttpUtils;
import com.fzm.mall.util.TimeUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.protocol.core.methods.response.Log;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ChainEventComponent {
    private static final String address_0 = "0x0000000000000000000000000000000000000000";
    private static final BigDecimal init_merchant_gas = new BigDecimal(5);
    private final UserMapper userMapper;
    private final UserAdminMapper userAdminMapper;
    private final GoodsSpuMapper goodsSpuMapper;
    private final GoodsSkuMapper goodsSkuMapper;
    private final UserAssetCollectionMapper userAssetCollectionMapper;
    private final UserAssetTokenMapper userAssetTokenMapper;
    private final OrderInfoMapper orderInfoMapper;
    private final OrderExpressMapper orderExpressMapper;
    private final OrderBlindBoxMapper orderBlindBoxMapper;
    private final SysHashUsedMapper sysHashUsedMapper;
    private final OrderComponent orderComponent;
    private final UserCacheComponent userCacheComponent;
    private final RedisIdComponent redisIdComponent;
    private final UserAdminService userAdminService;
    private final ContractProperties contractProperties;

    @Transactional(rollbackFor = Exception.class)
    public void handleLogs(List<Log> logs, ChainContractDO contractDO) {
        Map<String, TokenTemp> tempMap = new HashMap<>();

        for (Log ethLog : logs) {
            log.info("Chain event log, contract: {}, hash: {}", contractDO.getAddress(), ethLog.getTransactionHash());
            // 权限
            setAdminEvent(ethLog);
            setMerchantEvent(ethLog);
            freezeAccountEvent(ethLog);

            // 转账
            transferSingleEvent(ethLog, contractDO, tempMap);
            transferBatchEvent(ethLog, contractDO, tempMap);

            // 其他
            expressNFTEvent(ethLog, contractDO, tempMap);
            openBlindBoxNFTEvent(ethLog, contractDO, tempMap);
        }
    }

    private void setAdminEvent(Log ethLog) {
        FzmERC1155.SetAdminEventResponse eventResponse = FzmERC1155.getSetAdminEventFromLog(ethLog);
        if (eventResponse == null) {
            return;
        }
        String account = eventResponse.account;
        Boolean status = eventResponse.status;

        log.info("Set admin event, account: {}, status: {}", account, status);

        UserAdminDO updateAdminDO = new UserAdminDO();
        updateAdminDO.setAddress(account);
        updateAdminDO.setParentAddress(account);
        updateAdminDO.setRole(status ? UserEnum.RoleEnum.admin.getRole() : UserEnum.RoleEnum.none.getRole());
        updateAdminDO.setChildRole(UserEnum.ChildRoleEnum.none.getRole());
        updateAdminDO.setApplyStatus(UserEnum.ApplyStatusEnum.normal.getStatus());
        updateAdminDO.setProfitSharing(BigDecimal.ZERO);

        UserAdminDO selectDO = userAdminMapper.getByAddress(account);
        // 新增
        if (selectDO == null) {
            updateAdminDO.setStatus(UserEnum.StatusEnum.normal.getStatus());
            updateAdminDO.setNickname("");
            updateAdminDO.setHeadUrl("");
            updateAdminDO.setRegisterIp("");
            updateAdminDO.setRegisterTime(TimeUtils.nowTimestamp());
            try {
                userAdminMapper.register(updateAdminDO);
            } catch (Exception ignored) {
                return;
            }
            Wallet wallet = ETHUtils.newWallet(updateAdminDO.getUid(), IndexEnum.back);
            updateAdminDO.setInsideAddress(wallet.address());
            updateAdminDO.setInsidePrivateKey(wallet.privateKey());
            userAdminMapper.updateInsideByAddress(updateAdminDO);
        }
        // 编辑
        else {
            userAdminMapper.updateByAddress(updateAdminDO);
            userCacheComponent.delAddressToUserAdminDO(account);

            // 重置所有下级
            if (!status) {
                resetAllChild(selectDO.getAddress());
            }
        }
    }

    private void setMerchantEvent(Log ethLog) {
        FzmERC1155.SetMerchantEventResponse eventResponse = FzmERC1155.getSetMerchantEventFromLog(ethLog);
        if (eventResponse == null) {
            return;
        }
        String account = eventResponse.account;
        Boolean status = eventResponse.status;

        log.info("Set merchant event, account: {}, status: {}", account, status);

        UserAdminDO selectDO = userAdminMapper.getByInsideAddress(account);
        // 未注册过
        if (selectDO == null) {
            selectDO = userAdminMapper.getByAddress(account);
            if (selectDO == null) {
                log.error("Set merchant event, not found, account: {}", account);
            } else {
                log.error("Set merchant event, not found inside address, account: {}", account);
            }
        }
        // 注册过
        else {
            UserAdminDO updateDO = new UserAdminDO();
            updateDO.setAddress(selectDO.getAddress());
            updateDO.setParentAddress(selectDO.getAddress());
            updateDO.setRole(status ? UserEnum.RoleEnum.merchant.getRole() : UserEnum.RoleEnum.none.getRole());
            updateDO.setChildRole(UserEnum.ChildRoleEnum.none.getRole());
            updateDO.setApplyStatus(UserEnum.ApplyStatusEnum.normal.getStatus());
            updateDO.setProfitSharing(BigDecimal.ZERO);
            userAdminMapper.updateByAddress(updateDO);
            userCacheComponent.delAddressToUserAdminDO(selectDO.getAddress());

            // 首次设置给地址打初始燃料费
            if (status) {
//                TxResult<BigDecimal> txResult = Web3jUtils.getBalance(contractProperties.getChainUrl(), "", account, contractProperties.getDecimals());
//                if (txResult.getResult().compareTo(BigDecimal.ZERO) <= 0) {
//                    Web3jUtils.transfer(contractProperties.getChainUrl(),
//                            contractProperties.getChainId(),
//                            "",
//                            "",
//                            account,
//                            init_merchant_gas,
//                            contractProperties.getDecimals());
//                }

            }
            // 撤销商户，下架所有商品，重置所有下级
            else {
                resetAllChild(selectDO.getAddress());
                goodsSpuMapper.updateStatusByAddress(selectDO.getAddress(), GoodsEnum.SpuStatusEnum.mint_sell_ing.getStatus(), GoodsEnum.SpuStatusEnum.mint_success.getStatus());
            }
        }
    }

    private void resetAllChild(String address) {
        try {
            MUserAdminPageQO pageQO = new MUserAdminPageQO();
            pageQO.setParentAddress(address);
            List<UserAdminDO> adminDOS = userAdminMapper.listByPageQO(pageQO);
            for (UserAdminDO adminDO : adminDOS) {
                userAdminService.delChild(adminDO.getAddress());
            }
        } catch (Exception ignored) {

        }
    }

    private void freezeAccountEvent(Log ethLog) {
        FzmERC1155.FreezeAccountEventResponse eventResponse = FzmERC1155.getFreezeAccountEventFromLog(ethLog);
        if (eventResponse == null) {
            return;
        }
        String account = eventResponse.account;
        Boolean status = eventResponse.status;

        log.info("Freeze account event, account: {}, status: {}", account, status);

        // 冻结用户
        UserDO selectUserDO = userMapper.getByAddress(account);
        if (selectUserDO != null) {
            UserDO updateDO = new UserDO();
            updateDO.setAddress(account);
            updateDO.setStatus(status ? UserEnum.StatusEnum.frozen.getStatus() : UserEnum.StatusEnum.normal.getStatus());
            userMapper.updateByAddress(updateDO);
            userCacheComponent.delAddressToUserDO(account);
        }
        // 冻结管理员/商户
        UserAdminDO selectAdminDO = userAdminMapper.getByAddress(account);
        if (selectAdminDO == null) {
            selectAdminDO = userAdminMapper.getByInsideAddress(account);
        }
        if (selectAdminDO != null) {
            UserAdminDO updateDO = new UserAdminDO();
            updateDO.setAddress(selectAdminDO.getAddress());
            updateDO.setStatus(status ? UserEnum.StatusEnum.frozen.getStatus() : UserEnum.StatusEnum.normal.getStatus());
            userAdminMapper.updateByAddress(updateDO);
            userCacheComponent.delAddressToUserAdminDO(selectAdminDO.getAddress());
        }
    }

    private void transferSingleEvent(Log ethLog, ChainContractDO contractDO, Map<String, TokenTemp> tempMap) {
        FzmERC1155.TransferSingleEventResponse eventResponse = FzmERC1155.getTransferSingleEventFromLog(ethLog);
        if (eventResponse == null) {
            return;
        }
        long tokenId = eventResponse.id.longValue();

        log.info("Transfer single event, from: {}, to: {}, tokenId: {}", eventResponse.from, eventResponse.to, tokenId);

        handleAsset(false, eventResponse.from, tokenId, contractDO, tempMap);
        handleAsset(true, eventResponse.to, tokenId, contractDO, tempMap);
    }

    private void transferBatchEvent(Log ethLog, ChainContractDO contractDO, Map<String, TokenTemp> tempMap) {
        FzmERC1155.TransferBatchEventResponse eventResponse = FzmERC1155.getTransferBatchEventFromLog(ethLog);
        if (eventResponse == null) {
            return;
        }

        List<Long> tokenIds = eventResponse.ids.stream().map(BigInteger::longValue).toList();

        log.info("Transfer batch event, from: {}, to: {}, tokenIds: {}", eventResponse.from, eventResponse.to, tokenIds);

        for (Long tokenId : tokenIds) {
            handleAsset(false, eventResponse.from, tokenId, contractDO, tempMap);
            handleAsset(true, eventResponse.to, tokenId, contractDO, tempMap);
        }
    }

    private void handleAsset(boolean isTo, String account, long tokenId, ChainContractDO contractDO, Map<String, TokenTemp> tempMap) {
        if (address_0.equals(account) || contractDO.getAddress().equals(account)) {
            return;
        }

        UserAdminDO userAdminDO = userAdminMapper.getByInsideAddress(account);
        if (userAdminDO != null) {
            return;
        }

        TokenTemp temp = getPutTempMap(tokenId, contractDO, tempMap);
        if (temp.getMetaType() == MetaTypeEnum.unknown) {
            return;
        }

        TxResult<BigDecimal> balanceTr = ERC1155Manager.init(contractProperties.getChainUrl(), contractProperties.getChainId(), contractDO.getAddress()).balanceOf(account, tokenId);
        if (balanceTr.getStatus() != TxResultEnum.SUCCESS) {
            log.warn("Transfer handle asset get balance error. tr: {}", balanceTr);
            throw new RuntimeException("Transfer handle asset get balance error");
        }

        long nowTimestamp = TimeUtils.nowTimestamp();

        if (temp.getMetaType() == MetaTypeEnum.goods) {
            UserAssetTokenDO tokenDO = userAssetTokenMapper.getByAddressCtIdTokenId(account, contractDO.getCtId(), tokenId);
            if (tokenDO == null) {
                if (isTo) {
                    try {
                        userAssetTokenMapper.insert(temp.getGoodsId(), temp.getSkuId(), account, contractDO.getCtId(), tokenId, balanceTr.getResult(), nowTimestamp);
                    } catch (Exception ignored) {
                    }
                }
            } else {
                userAssetTokenMapper.updateNum(temp.getGoodsId(), temp.getSkuId(), account, contractDO.getCtId(), tokenId, balanceTr.getResult(), nowTimestamp);
            }

            UserAssetCollectionDO collectionDO = userAssetCollectionMapper.getByGoodsIdAddress(temp.getGoodsId(), account);
            if (collectionDO == null) {
                if (isTo) {
                    try {
                        userAssetCollectionMapper.insert(temp.getGoodsId(), account, nowTimestamp);
                    } catch (Exception ignored) {
                    }
                }
            } else {
                userAssetCollectionMapper.updateNum(temp.getGoodsId(), account, nowTimestamp);
            }
        }
    }

    private void expressNFTEvent(Log ethLog, ChainContractDO contractDO, Map<String, TokenTemp> tempMap) {
        FzmERC1155.ExpressNFTEventResponse eventResponse = FzmERC1155.getExpressNFTEventFromLog(ethLog);
        if (eventResponse == null) {
            return;
        }

        String txHash = ethLog.getTransactionHash();

        String from = eventResponse.from;
        List<Long> tokenIds = eventResponse.ids.stream().map(BigInteger::longValue).toList();
        String detail = eventResponse.detail;

        log.info("Express NTF event, from: {}, tokenIds: {}", from, tokenIds);

        TokenTemp temp = getPutTempMap(tokenIds.get(0), contractDO, tempMap);
        if (temp.getMetaType() == MetaTypeEnum.unknown) {
            return;
        }

        String selectTxHash = sysHashUsedMapper.getByTxHash(txHash);
        if (StringUtils.isNotBlank(selectTxHash)) {
            return;
        }

        long nowTimestamp = TimeUtils.nowTimestamp();
        try {
            sysHashUsedMapper.insert(txHash, nowTimestamp);
        } catch (Exception e) {
            return;
        }

        String decodeDetail = AesUtils.decode(detail);
        OrderExpressDO expressDO = JSON.parseObject(decodeDetail, OrderExpressDO.class);
        if (expressDO == null) {
            return;
        }

        if (StringUtils.isBlank(expressDO.getNote())) {
            expressDO.setNote("");
        }

        String orderId = redisIdComponent.nextOrderId();

        OrderExpressRefundNoteDO noteObj = new OrderExpressRefundNoteDO();
        noteObj.setType("user");
        noteObj.setTime(TimeUtils.nowTimestamp());
        noteObj.setNote("apply");
        noteObj.setToStatus(OrderEnum.ExpressStatusEnum.wait.getStatus());

        JSONArray refundNoteAry = new JSONArray();
        refundNoteAry.add(noteObj);


        expressDO.setOrderId(orderId);
        expressDO.setSellerAddress(temp.getAddress());
        expressDO.setBuyerAddress(from);
        expressDO.setGoodsId(temp.getGoodsId());
        expressDO.setSkuId(temp.getSkuId());
        expressDO.setCtId(contractDO.getCtId());
        expressDO.setTokenIdJson(JSON.toJSONString(tokenIds));
        expressDO.setNum(tokenIds.size());
        expressDO.setTxHash(txHash);
        expressDO.setStatus(OrderEnum.ExpressStatusEnum.wait.getStatus());
        expressDO.setExpressCode("");
        expressDO.setOrderTime(nowTimestamp);
        expressDO.setRefundNote(refundNoteAry.toJSONString());
        expressDO.setRefundTxStatus(OrderEnum.TxStatusEnum.wait.getStatus());
        expressDO.setRefundTxHash("");
        expressDO.setRefundTxNote("");
        expressDO.setDeleteStatus(CommonEnum.BoolEnum.NO.getStatus());
        try {
            orderExpressMapper.insert(expressDO);
        } catch (Exception ignored) {
            return;
        }

        // 直接提货的订单
        JSONObject expressDetail = JSON.parseObject(decodeDetail);
        int expressType = expressDetail.getIntValue("expressType", CommonEnum.BoolEnum.NO.getStatus());
        if (expressType == CommonEnum.BoolEnum.NO.getStatus()) {
            return;
        }
        String orderInfoId = expressDetail.getString("orderId");
        if (StringUtils.isBlank(orderInfoId)) {
            return;
        }
        OrderInfoDO orderInfoDO = orderInfoMapper.getByOrderId(orderInfoId);
        if (orderInfoDO == null) {
            return;
        }
        List<Long> infoTokenIds = JSON.parseArray(orderInfoDO.getTokenIdJson(), Long.class);
        if (tokenIds.size() != infoTokenIds.size() || new HashSet<>(tokenIds).containsAll(infoTokenIds)) {
            return;
        }

        // TODO 活动

    }

    private void openBlindBoxNFTEvent(Log ethLog, ChainContractDO contractDO, Map<String, TokenTemp> tempMap) {
        FzmERC1155.OpenBlindBoxNFTEventResponse eventResponse = FzmERC1155.getOpenBlindBoxNFTEventFromLog(ethLog);
        if (eventResponse == null) {
            return;
        }

        String txHash = ethLog.getTransactionHash();
        String from = eventResponse.from;
        long tokenId = eventResponse.id.longValue();

        log.info("Open Blind Box NTF event, from: {}, tokenId: {}", from, tokenId);

        TokenTemp temp = getPutTempMap(tokenId, contractDO, tempMap);
        if (temp.getMetaType() == MetaTypeEnum.unknown) {
            return;
        }

        String selectTxHash = sysHashUsedMapper.getByTxHash(txHash);
        if (StringUtils.isNotBlank(selectTxHash)) {
            return;
        }

        long nowTimestamp = TimeUtils.nowTimestamp();
        try {
            sysHashUsedMapper.insert(txHash, nowTimestamp);
        } catch (Exception e) {
            return;
        }

        GoodsSkuDO rewardSku;
        do {
            try {
                rewardSku = orderComponent.openBlindBox(temp.getGoodsId());
            } catch (Exception e) {
                rewardSku = null;
            }
        } while (rewardSku == null);

        String orderId = redisIdComponent.nextOrderId();
        List<Long> tokenIds = redisIdComponent.getTokenIds(rewardSku.getSkuId(), rewardSku.getTokenPrefix(), 1);

        OrderBlindBoxDO boxDO = new OrderBlindBoxDO();
        boxDO.setOrderId(orderId);
        boxDO.setSellerAddress(temp.getAddress());
        boxDO.setBuyerAddress(from);
        boxDO.setGoodsId(temp.getGoodsId());
        boxDO.setSkuId(temp.getSkuId());
        boxDO.setCtId(contractDO.getCtId());
        boxDO.setTokenId(tokenId);
        boxDO.setTxHash(txHash);
        boxDO.setRewardSkuId(rewardSku.getSkuId());
        boxDO.setRewardTokenId(tokenIds.get(0));
        boxDO.setRewardTxHash("");
        boxDO.setRewardTxNote("");
        boxDO.setRewardTxStatus(OrderEnum.TxStatusEnum.wait.getStatus());
        boxDO.setOrderTime(nowTimestamp);

        try {
            orderBlindBoxMapper.insert(boxDO);
        } catch (Exception ignored) {
        }

    }

    private TokenTemp getPutTempMap(long tokenId, ChainContractDO contractDO, Map<String, TokenTemp> tempMap) {
        String skuKey = contractDO.getCtId() + "_" + tokenId;
        TokenTemp temp = tempMap.get(skuKey);
        if (temp != null) {
            return temp;
        }

        TxResult<String> tr = ERC1155Manager.init(contractProperties.getChainUrl(), contractProperties.getChainId(), contractDO.getAddress()).uri(tokenId);
        if (tr.getStatus() != TxResultEnum.SUCCESS) {
            log.warn("Chain event get token uri failed, contract: {}, tokenId: {}, tr: {}", contractDO.getAddress(), tokenId, tr);
            throw new RuntimeException("Chain event get token uri failed");
        }

        String execute;
        try {
            execute = HttpUtils.url(tr.getResult()).execute();
        } catch (Exception e) {
            log.warn("Chain event get token uri properties error, contract: {}, tokenId: {}", contractDO.getAddress(), tokenId, e);
            throw new RuntimeException("Chain event get token uri properties error");
        }

        try {
            TokenUriProperties properties = JSON.parseObject(execute, TokenUri.class).getProperties();

            if (properties.getMetaType() == MetaTypeEnum.goods.getType()) {
                GoodsSkuDO skuDO = goodsSkuMapper.getBySkuId(properties.getMetaId());
                if (skuDO != null) {
                    temp = new TokenTemp();
                    temp.setMetaType(MetaTypeEnum.goods);
                    temp.setAddress(skuDO.getAddress());
                    temp.setGoodsId(skuDO.getGoodsId());
                    temp.setSkuId(skuDO.getSkuId());
                }
            }
        } catch (Exception e) {
            log.warn("Chain event parse token properties error, contract: {}, tokenId: {}", contractDO.getAddress(), tokenId, e);
        }

        if (temp == null) {
            log.warn("Chain event token type unknown, contract: {}, tokenId: {}", contractDO.getAddress(), tokenId);
            temp = new TokenTemp();
            temp.setMetaType(MetaTypeEnum.unknown);
        }

        tempMap.put(skuKey, temp);

        return temp;
    }

    @Data
    static class TokenTemp {
        private MetaTypeEnum metaType;
        private String address;
        private String goodsId;
        private String skuId;
    }
}
