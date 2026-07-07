package com.fzm.mall.crontab;

import com.fzm.mall.component.AirdropComponent;
import com.fzm.mall.constant.enums.AirdropEnum;
import com.fzm.mall.constant.enums.CommonEnum;
import com.fzm.mall.entity.dataobject.AirdropInfoDO;
import com.fzm.mall.entity.dataobject.UserAdminDO;
import com.fzm.mall.mapper.AirdropInfoMapper;
import com.fzm.mall.mapper.UserAdminMapper;
import com.fzm.mall.redis.RedisCacheExpireEnum;
import com.fzm.mall.redis.RedisLockComponent;
import com.fzm.mall.redis.key.RedisLockKey;
import com.fzm.mall.third.chain.entity.TxResult;
import com.fzm.mall.third.chain.entity.TxResultEnum;
import com.fzm.mall.util.TimeUtils;
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
public class AirdropCrontab {
    private final AirdropInfoMapper airdropInfoMapper;
    private final UserAdminMapper userAdminMapper;
    private final RedisLockComponent redisLockComponent;
    private final AirdropComponent airdropComponent;

    @Scheduled(cron = "0/5 * * * * ?")
    public void checkStock() {
        List<AirdropInfoDO> infoDOS = airdropInfoMapper.listByStatus(CommonEnum.BoolEnum.YES.getStatus(), AirdropEnum.TaskStatusEnum.wait.getStatus(), TimeUtils.nowTimestamp());
        if (CollectionUtils.isEmpty(infoDOS)) {
            return;
        }

        for (AirdropInfoDO infoDO : infoDOS) {
            String lockKey = String.format(RedisLockKey.Airdrop.info_id, infoDO.getInfoId());
            boolean lock = redisLockComponent.lock(lockKey, lockKey, RedisCacheExpireEnum.expire_minutes_10);
            if (!lock) {
                continue;
            }

            log.info("空投定时器，库存检测，执行开始，空投编号：{}", infoDO.getInfoId());

            AirdropEnum.TaskStatusEnum taskStatusEnum = AirdropEnum.TaskStatusEnum.check_stock;
            String txNote = "";
            try {
                airdropComponent.checkStock(infoDO);
            } catch (Exception e) {
                taskStatusEnum = AirdropEnum.TaskStatusEnum.wait;
                txNote = e.getMessage();
            }

            AirdropInfoDO updateInfoDO = new AirdropInfoDO();
            updateInfoDO.setInfoId(infoDO.getInfoId());
            // 如果库存检测失败，将空投设置为未上架
            if (taskStatusEnum == AirdropEnum.TaskStatusEnum.wait) {
                updateInfoDO.setStatus(CommonEnum.BoolEnum.NO.getStatus());
            }
            updateInfoDO.setTaskStatus(taskStatusEnum.getStatus());
            updateInfoDO.setTxNote(txNote);
            updateInfoDO.setOriginalTaskStatus(AirdropEnum.TaskStatusEnum.wait.getStatus());
            airdropInfoMapper.updateByInfoId(updateInfoDO);

            redisLockComponent.unlock(lockKey, lockKey);

            log.info("空投定时器，库存检测，执行结束，空投编号：{}，执行结果：{}", infoDO.getInfoId(), taskStatusEnum == AirdropEnum.TaskStatusEnum.check_stock ? "成功" : txNote);
        }
    }

    @Scheduled(cron = "0/5 * * * * ?")
    public void assignToken() {
        List<AirdropInfoDO> infoDOS = airdropInfoMapper.listByStatus(CommonEnum.BoolEnum.YES.getStatus(), AirdropEnum.TaskStatusEnum.check_stock.getStatus(), TimeUtils.nowTimestamp());
        if (CollectionUtils.isEmpty(infoDOS)) {
            return;
        }

        for (AirdropInfoDO infoDO : infoDOS) {
            String lockKey = String.format(RedisLockKey.Airdrop.info_id, infoDO.getInfoId());
            boolean lock = redisLockComponent.lock(lockKey, lockKey, RedisCacheExpireEnum.expire_minutes_10);
            if (!lock) {
                continue;
            }

            log.info("空投定时器，分配token，执行开始，空投编号：{}", infoDO.getInfoId());

            airdropComponent.assignToken(infoDO);

            AirdropInfoDO updateInfoDO = new AirdropInfoDO();
            updateInfoDO.setInfoId(infoDO.getInfoId());
            updateInfoDO.setTaskStatus(AirdropEnum.TaskStatusEnum.assign_token.getStatus());
            updateInfoDO.setOriginalTaskStatus(AirdropEnum.TaskStatusEnum.check_stock.getStatus());
            airdropInfoMapper.updateByInfoId(updateInfoDO);

            redisLockComponent.unlock(lockKey, lockKey);

            log.info("空投定时器，分配token，执行结束，空投编号：{}", infoDO.getInfoId());
        }
    }

    @Scheduled(cron = "0/5 * * * * ?")
    public void airdrop() {
        List<AirdropInfoDO> infoDOS = airdropInfoMapper.listByStatus(CommonEnum.BoolEnum.YES.getStatus(), AirdropEnum.TaskStatusEnum.assign_token.getStatus(), TimeUtils.nowTimestamp());
        if (CollectionUtils.isEmpty(infoDOS)) {
            return;
        }

        for (AirdropInfoDO infoDO : infoDOS) {
            UserAdminDO userAdminDO = userAdminMapper.getByAddress(infoDO.getAddress());

            String lockKey = String.format(RedisLockKey.Chain.nonce_of_address, userAdminDO.getInsideAddress());
            boolean lock = redisLockComponent.lock(lockKey, lockKey, RedisCacheExpireEnum.expire_minutes_10);
            if (!lock) {
                continue;
            }

            log.info("空投定时器，空投token，执行开始，空投编号：{}", infoDO.getInfoId());

            TxResult<String> txResult = airdropComponent.airdrop(infoDO, userAdminDO);

            AirdropInfoDO updateInfoDO = new AirdropInfoDO();
            updateInfoDO.setInfoId(infoDO.getInfoId());
            updateInfoDO.setOriginalTaskStatus(AirdropEnum.TaskStatusEnum.assign_token.getStatus());
            if (txResult.getStatus() == TxResultEnum.FAILED) {
                updateInfoDO.setTaskStatus(AirdropEnum.TaskStatusEnum.fail.getStatus());
                updateInfoDO.setTxNote(txResult.getError());
            } else {
                updateInfoDO.setTaskStatus(AirdropEnum.TaskStatusEnum.success.getStatus());
                updateInfoDO.setTxHash(txResult.getResult());
                updateInfoDO.setTxNote("");
            }

            airdropInfoMapper.updateByInfoId(updateInfoDO);

            redisLockComponent.unlock(lockKey, lockKey);

            log.info("空投定时器，空投token，执行结束，空投编号：{}，执行结果：{}", infoDO.getInfoId(), txResult);
        }

    }
}
