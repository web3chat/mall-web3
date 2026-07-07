package com.fzm.mall.crontab;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fzm.mall.constant.enums.AssetEnum;
import com.fzm.mall.constant.enums.ChainEnum;
import com.fzm.mall.entity.dataobject.UserAdminDO;
import com.fzm.mall.entity.dataobject.UserBackAssetChangeHistoryDO;
import com.fzm.mall.entity.properties.WithdrawProperties;
import com.fzm.mall.mapper.UserBackAssetChangeHistoryMapper;
import com.fzm.mall.redis.RedisCacheExpireEnum;
import com.fzm.mall.redis.RedisLockComponent;
import com.fzm.mall.redis.key.RedisLockKey;
import com.fzm.mall.service.UserAdminService;
import com.fzm.mall.service.UserBackAssetService;
import com.fzm.mall.third.chain.entity.TxResult;
import com.fzm.mall.third.chain.entity.TxResultEnum;
import com.fzm.mall.third.chain.util.Web3jUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WithdrawCrontab {
    private final UserBackAssetChangeHistoryMapper userBackAssetChangeHistoryMapper;
    private final UserAdminService userAdminService;
    private final UserBackAssetService userBackAssetService;
    private final RedisLockComponent redisLockComponent;

    private final WithdrawProperties withdrawProperties;

    // 后台提现
    @Scheduled(cron = "0/5 * * * * ?")
    public void backTransfer() {
        List<UserBackAssetChangeHistoryDO> historyDOS = userBackAssetChangeHistoryMapper.listByLogType(AssetEnum.LogTypeEnum.withdraw.getType());
        for (UserBackAssetChangeHistoryDO historyDO : historyDOS) {
            ChainEnum.CoinEnum coinEnum = ChainEnum.CoinEnum.exist(historyDO.getCoinType());
            WithdrawProperties.Withdraw withdraw = withdrawProperties.getByCoinEnum(coinEnum);
            if (withdraw == null) {
                continue;
            }
            UserAdminDO adminDO = userAdminService.getByAddress(historyDO.getAddress());

            String lockKey = String.format(RedisLockKey.Chain.nonce_of_address, adminDO.getInsideAddress());
            boolean lock = redisLockComponent.lock(lockKey, lockKey, RedisCacheExpireEnum.expire_minutes_10);
            if (!lock) {
                continue;
            }

            TxResult<String> txResult = Web3jUtils.transfer(withdraw.getChainUrl(),
                    withdraw.getChainId(),
                    withdraw.getContractAddress(),
                    adminDO.getInsidePrivateKey(),
                    historyDO.getAddress(),
                    historyDO.getNumber().abs(),
                    withdraw.getDecimals());

            log.info("后台提现转币，logId: {} , result: {}", historyDO.getLogId(), txResult);

            try {
                JSONObject extendData = JSON.parseObject(historyDO.getExtendData());
                // 失败
                if (txResult.getStatus() == TxResultEnum.FAILED) {
                    extendData.put("txNote", txResult.getError());

                    userBackAssetService.withdrawTransferFail(historyDO, extendData);
                }
                // 成功
                else if (txResult.getStatus() == TxResultEnum.SUCCESS) {
                    extendData.put("txHash", txResult.getResult());

                    userBackAssetService.withdrawTransferConfirm(historyDO, extendData);
                }
            } catch (Exception e) {
                log.warn("后台提现转币异常，logId: {}", historyDO.getLogId(), e);
            } finally {
                redisLockComponent.unlock(lockKey, lockKey);
            }
        }
    }

    @Scheduled(cron = "0/5 * * * * ?")
    public void backConfirm() {
        List<UserBackAssetChangeHistoryDO> historyDOS = userBackAssetChangeHistoryMapper.listByLogType(AssetEnum.LogTypeEnum.withdraw_chain_confirm.getType());

        for (UserBackAssetChangeHistoryDO historyDO : historyDOS) {
            ChainEnum.CoinEnum coinEnum = ChainEnum.CoinEnum.exist(historyDO.getCoinType());
            WithdrawProperties.Withdraw withdraw = withdrawProperties.getByCoinEnum(coinEnum);
            if (withdraw == null) {
                continue;
            }

            String lockKey = String.format(RedisLockKey.Withdraw.back_confirm, historyDO.getLogId());
            boolean lock = redisLockComponent.lock(lockKey, lockKey, RedisCacheExpireEnum.expire_minutes_10);
            if (!lock) {
                continue;
            }

            String txHash = JSON.parseObject(historyDO.getExtendData()).getString("txHash");

            TxResult<TransactionReceipt> txResult = Web3jUtils.ethGetTransactionReceipt(withdraw.getChainUrl(), txHash);
            // 尚未打包 / 打包还未确认
            if (txResult.getStatus() == TxResultEnum.RUNNING) {
                redisLockComponent.unlock(lockKey, lockKey);
                continue;
            }

            log.info("后台提现转币确认，logId: {} , result: {}", historyDO.getLogId(), txResult);

            try {
                JSONObject extendData = JSON.parseObject(historyDO.getExtendData());

                // 失败
                if (txResult.getStatus() == TxResultEnum.FAILED) {
                    extendData.put("txNote", txResult.getError());

                    userBackAssetService.withdrawConfirmFail(historyDO, extendData);
                }
                // 成功
                else {
                    userBackAssetService.withdrawConfirmSuccess(historyDO, extendData);
                }
            } catch (Exception e) {
                log.warn("后台提现转币确认异常，logId: {}", historyDO.getLogId(), e);
            } finally {
                redisLockComponent.unlock(lockKey, lockKey);
            }

        }

    }
}
