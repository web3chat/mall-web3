package com.fzm.mall.crontab;

import com.fzm.mall.component.ChainEventComponent;
import com.fzm.mall.entity.dataobject.ChainContractDO;
import com.fzm.mall.entity.properties.ContractProperties;
import com.fzm.mall.mapper.ChainContractMapper;
import com.fzm.mall.redis.RedisCacheExpireEnum;
import com.fzm.mall.redis.RedisLockComponent;
import com.fzm.mall.redis.cache.ChainEventCacheComponent;
import com.fzm.mall.redis.key.RedisLockKey;
import com.fzm.mall.third.chain.contract.FzmERC1155;
import com.fzm.mall.third.chain.entity.TxResult;
import com.fzm.mall.third.chain.entity.TxResultEnum;
import com.fzm.mall.third.chain.util.Web3jUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.web3j.abi.EventEncoder;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;

import java.math.BigInteger;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ChainEventCrontab {
    private static final BigInteger sync_number_limit = BigInteger.valueOf(10);
    private static final String[] topics = {
            // 权限
            EventEncoder.encode(FzmERC1155.SETADMIN_EVENT),
            EventEncoder.encode(FzmERC1155.SETMERCHANT_EVENT),
            EventEncoder.encode(FzmERC1155.FREEZEACCOUNT_EVENT),
            // 转账
            EventEncoder.encode(FzmERC1155.TRANSFERSINGLE_EVENT),
            EventEncoder.encode(FzmERC1155.TRANSFERBATCH_EVENT),
            // 其他
            EventEncoder.encode(FzmERC1155.EXPRESSNFT_EVENT),
            EventEncoder.encode(FzmERC1155.OPENBLINDBOXNFT_EVENT),
    };
    private final ChainContractMapper chainContractMapper;
    private final ChainEventComponent chainEventComponent;
    private final RedisLockComponent redisLockComponent;
    private final ChainEventCacheComponent chainEventCacheComponent;
    private final ContractProperties contractProperties;

    @Scheduled(cron = "0/5 * * * * ?")
    public void listener() {
        List<ChainContractDO> contractDOS = chainContractMapper.list();
        if (CollectionUtils.isEmpty(contractDOS)) {
            return;
        }

        String lockKey = RedisLockKey.Chain.event_listener;
        boolean lock = redisLockComponent.lock(lockKey, lockKey, RedisCacheExpireEnum.expire_minutes_10);
        if (!lock) {
            return;
        }

        // 已经确认过的高度
        BigInteger confirmBlockNumber = chainEventCacheComponent.getConfirmHeight();
        if (confirmBlockNumber == null) {
            redisLockComponent.unlock(lockKey, lockKey);
            return;
        }

        // 最新高度
        TxResult<BigInteger> trBlockNumber = Web3jUtils.ethBlockNumber(contractProperties.getChainUrl());
        if (trBlockNumber.getStatus() == TxResultEnum.FAILED) {
            redisLockComponent.unlock(lockKey, lockKey);
            return;
        }
        // 等待10个区块确认
        BigInteger latestBlockNumber = trBlockNumber.getResult().subtract(BigInteger.TEN);

        // 初始化为最新高度
        if (confirmBlockNumber.compareTo(BigInteger.ZERO) == 0) {
            chainEventCacheComponent.setConfirmHeight(latestBlockNumber.subtract(BigInteger.TEN));
            redisLockComponent.unlock(lockKey, lockKey);
            return;
        }

        if (latestBlockNumber.compareTo(confirmBlockNumber) <= 0) {
            redisLockComponent.unlock(lockKey, lockKey);
            return;
        }

        // 高度区间
        BigInteger startBlockNumber = confirmBlockNumber.add(BigInteger.ONE);
        BigInteger gapNum = latestBlockNumber.subtract(startBlockNumber);
        if (gapNum.compareTo(sync_number_limit) > 0) {
            gapNum = sync_number_limit;
        }
        BigInteger endBlockNumber = startBlockNumber.add(gapNum);

        for (ChainContractDO contractDO : contractDOS) {
            if (StringUtils.isBlank(contractDO.getAddress())) {
                continue;
            }
            // 过滤器
            EthFilter filter = new EthFilter(new DefaultBlockParameterNumber(startBlockNumber), new DefaultBlockParameterNumber(endBlockNumber), contractDO.getAddress()).addOptionalTopics(topics);

            TxResult<EthLog> trEthLog = Web3jUtils.ethGetLogs(contractProperties.getChainUrl(), filter);
            if (trEthLog.getStatus() == TxResultEnum.FAILED) {
                redisLockComponent.unlock(lockKey, lockKey);
                return;
            }

            try {
                List<Log> logs = trEthLog.getResult().getLogs().stream()
                        .filter(logResult -> logResult instanceof EthLog.LogObject)
                        .map(logResult -> (EthLog.LogObject) logResult)
                        .map(EthLog.LogObject::get)
                        .toList();
                chainEventComponent.handleLogs(logs, contractDO);
            } catch (Exception e) {
                log.warn("Chain Event Listener. HandleLogs failed.", e);
                redisLockComponent.unlock(lockKey, lockKey);
                return;
            }
        }

        chainEventCacheComponent.setConfirmHeight(endBlockNumber);
        redisLockComponent.unlock(lockKey, lockKey);
    }

}
