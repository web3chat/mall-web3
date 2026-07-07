package com.fzm.mall.service;

import com.alibaba.fastjson2.JSONObject;
import com.fzm.mall.constant.enums.AssetEnum;
import com.fzm.mall.constant.enums.CommonEnum;
import com.fzm.mall.constant.response.ResponseEnum;
import com.fzm.mall.entity.dataobject.UserBackAssetChangeHistoryDO;
import com.fzm.mall.entity.dataobject.UserBackAssetDO;
import com.fzm.mall.entity.queryobject.back.MUserBackAssetLogPageQO;
import com.fzm.mall.entity.queryobject.back.MUserBackAssetPageQO;
import com.fzm.mall.mapper.UserBackAssetChangeHistoryMapper;
import com.fzm.mall.mapper.UserBackAssetMapper;
import com.fzm.mall.util.AssertUtils;
import com.fzm.mall.util.TimeUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserBackAssetService {
    private final UserBackAssetMapper userBackAssetMapper;
    private final UserBackAssetChangeHistoryMapper userBackAssetChangeHistoryMapper;

    @Transactional(rollbackFor = Exception.class)
    public void orderFreeze(String address, Integer coinType, BigDecimal changeBalance, JSONObject extendData) {
        changeBalance(address, coinType, null, changeBalance.abs(), null, AssetEnum.LogTypeEnum.order_freeze, extendData, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void orderUnfreeze(String address, Integer coinType, BigDecimal changeBalance, JSONObject extendData) {
        changeBalance(address, coinType, changeBalance.abs(), changeBalance.abs().negate(), null, AssetEnum.LogTypeEnum.order_unfreeze, extendData, null);
    }

    /**
     * 提现申请
     */
    @Transactional(rollbackFor = Exception.class)
    public void withdraw(String address, Integer coinType, BigDecimal changeBalance) {
        changeBalance(address, coinType, changeBalance.abs().negate(), null, null, AssetEnum.LogTypeEnum.withdraw, null, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void withdrawTransferFail(UserBackAssetChangeHistoryDO historyDO, JSONObject extendData) {
        changeBalance(historyDO.getAddress(), historyDO.getCoinType(), historyDO.getNumber().abs(), null, AssetEnum.LogTypeEnum.withdraw, AssetEnum.LogTypeEnum.withdraw_chain_fail, extendData, historyDO.getLogId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void withdrawTransferConfirm(UserBackAssetChangeHistoryDO historyDO, JSONObject extendData) {
        changeBalance(historyDO.getAddress(), historyDO.getCoinType(), null, null, AssetEnum.LogTypeEnum.withdraw, AssetEnum.LogTypeEnum.withdraw_chain_confirm, extendData, historyDO.getLogId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void withdrawConfirmFail(UserBackAssetChangeHistoryDO historyDO, JSONObject extendData) {
        changeBalance(historyDO.getAddress(), historyDO.getCoinType(), historyDO.getNumber().abs(), null, AssetEnum.LogTypeEnum.withdraw_chain_confirm, AssetEnum.LogTypeEnum.withdraw_chain_fail, extendData, historyDO.getLogId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void withdrawConfirmSuccess(UserBackAssetChangeHistoryDO historyDO, JSONObject extendData) {
        changeBalance(historyDO.getAddress(), historyDO.getCoinType(), null, null, AssetEnum.LogTypeEnum.withdraw_chain_confirm, AssetEnum.LogTypeEnum.withdraw_chain_success, extendData, historyDO.getLogId());
    }

    private void changeBalance(String address, Integer coinType, BigDecimal changeBalance, BigDecimal changeFrozenBalance,
                               AssetEnum.LogTypeEnum fromLogTypeEnum, AssetEnum.LogTypeEnum toLogTypeEnum,
                               JSONObject extendData, Long logId) {

        changeBalance = changeBalance == null ? BigDecimal.ZERO : changeBalance;
        changeFrozenBalance = changeFrozenBalance == null ? BigDecimal.ZERO : changeFrozenBalance;

        // 修改余额
        if (changeBalance.compareTo(BigDecimal.ZERO) != 0 || changeFrozenBalance.compareTo(BigDecimal.ZERO) != 0) {
            UserBackAssetDO selectAssetDO = getAssetByAddressCoinType(address, coinType);
            int ok;
            if (selectAssetDO == null) {
                try {
                    ok = userBackAssetMapper.insert(address, coinType, changeBalance, changeFrozenBalance);
                } catch (Exception e) {
                    ok = 0;
                }
            } else {
                ok = userBackAssetMapper.updateBalance(address, coinType, changeBalance, changeFrozenBalance);
            }
            AssertUtils.isTrue(ok == 1, ResponseEnum.failure);
        }

        // 提现：除了申请是新增日志，其他都是修改指定日志
        if (toLogTypeEnum.getWithdrawType() == CommonEnum.BoolEnum.YES && toLogTypeEnum != AssetEnum.LogTypeEnum.withdraw) {
            AssertUtils.isNotNull(logId, ResponseEnum.failure);
            AssertUtils.isNotNull(fromLogTypeEnum, ResponseEnum.failure);
            int ok = userBackAssetChangeHistoryMapper.updateLogTypeByLogId(logId, fromLogTypeEnum.getType(), toLogTypeEnum.getType(), extendData == null ? null : extendData.toJSONString());
            AssertUtils.isTrue(ok == 1, ResponseEnum.failure);
        }
        // 其他：新增日志
        else {
            UserBackAssetChangeHistoryDO historyDO = new UserBackAssetChangeHistoryDO();
            historyDO.setAddress(address);
            historyDO.setCoinType(coinType);
            historyDO.setLogType(toLogTypeEnum.getType());
            historyDO.setWithdrawType(toLogTypeEnum.getWithdrawType().getStatus());
            historyDO.setCreateTime(TimeUtils.nowTimestamp());
            historyDO.setExtendData(extendData == null ? "{}" : extendData.toJSONString());

            if (changeFrozenBalance.compareTo(BigDecimal.ZERO) != 0) {
                historyDO.setNumber(changeFrozenBalance);
                historyDO.setBalanceType(AssetEnum.BalanceTypeEnum.frozen.getType());
                int ok = userBackAssetChangeHistoryMapper.insert(historyDO);
                AssertUtils.isTrue(ok == 1, ResponseEnum.failure);
            }
            if (changeBalance.compareTo(BigDecimal.ZERO) != 0) {
                historyDO.setNumber(changeBalance);
                historyDO.setBalanceType(AssetEnum.BalanceTypeEnum.balance.getType());
                int ok = userBackAssetChangeHistoryMapper.insert(historyDO);
                AssertUtils.isTrue(ok == 1, ResponseEnum.failure);
            }
        }
    }

    public UserBackAssetDO getAssetByAddressCoinType(String address, Integer coinType) {
        if (StringUtils.isBlank(address) || coinType == null) {
            return null;
        }
        return userBackAssetMapper.getAssetByAddressCoinType(address, coinType);
    }

    public PageInfo<UserBackAssetDO> pageAsset(MUserBackAssetPageQO pageQO) {
        PageHelper.startPage(pageQO.getPage(), pageQO.getSize());
        List<UserBackAssetDO> dos = userBackAssetMapper.listByPageQO(pageQO);
        return new PageInfo<>(dos);
    }

    public PageInfo<UserBackAssetChangeHistoryDO> pageChangeHistory(MUserBackAssetLogPageQO pageQO) {
        PageHelper.startPage(pageQO.getPage(), pageQO.getSize());
        List<UserBackAssetChangeHistoryDO> dos = userBackAssetChangeHistoryMapper.listByPageQO(pageQO);
        return new PageInfo<>(dos);
    }
}
