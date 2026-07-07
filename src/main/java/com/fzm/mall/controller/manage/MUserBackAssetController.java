package com.fzm.mall.controller.manage;

import com.fzm.mall.constant.enums.ChainEnum;
import com.fzm.mall.constant.enums.UserEnum;
import com.fzm.mall.constant.response.ResponseEnum;
import com.fzm.mall.constant.response.ResponseUtils;
import com.fzm.mall.constant.response.ResponseVO;
import com.fzm.mall.entity.common.PageVO;
import com.fzm.mall.entity.common.ThreadInfo;
import com.fzm.mall.entity.dataobject.UserAdminDO;
import com.fzm.mall.entity.dataobject.UserBackAssetChangeHistoryDO;
import com.fzm.mall.entity.dataobject.UserBackAssetDO;
import com.fzm.mall.entity.properties.ChainPaymentProperties;
import com.fzm.mall.entity.properties.WithdrawProperties;
import com.fzm.mall.entity.queryobject.back.MUserBackAssetLogPageQO;
import com.fzm.mall.entity.queryobject.back.MUserBackAssetPageQO;
import com.fzm.mall.entity.requestobject.back.MCoinNumberRO;
import com.fzm.mall.entity.viewobject.back.MUserBackAssetChangeHistoryVO;
import com.fzm.mall.entity.viewobject.back.MUserBackAssetVO;
import com.fzm.mall.service.UserAdminService;
import com.fzm.mall.service.UserBackAssetService;
import com.fzm.mall.third.chain.entity.TxResult;
import com.fzm.mall.third.chain.util.Web3jUtils;
import com.fzm.mall.util.AssertUtils;
import com.fzm.mall.util.BeanCopierUtils;
import com.fzm.mall.util.PageVOUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "用户-后台-资产")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(value = "/m/user-back/asset", produces = MediaType.APPLICATION_JSON_VALUE)
public class MUserBackAssetController {
    private final UserBackAssetService userBackAssetService;
    private final UserAdminService userAdminService;
    private final WithdrawProperties withdrawProperties;
    private final ChainPaymentProperties chainPaymentProperties;

    @Operation(summary = "资产列表分页")
    @PostMapping("/page")
    public ResponseVO<PageVO<MUserBackAssetVO>> page(@RequestBody MUserBackAssetPageQO pageQO) {
        if (ThreadInfo.getInfo().getRole() == UserEnum.RoleEnum.merchant.getRole()) {
            pageQO.setAddress(ThreadInfo.getInfo().getParentAddress());
        }

        PageInfo<UserBackAssetDO> pageInfo = userBackAssetService.pageAsset(pageQO);

        List<UserBackAssetDO> list = pageInfo.getList();
        if (list.isEmpty()) {
            UserBackAssetDO assetDO = new UserBackAssetDO();
            assetDO.setAddress(ThreadInfo.getInfo().getParentAddress());
            assetDO.setCoinType(ChainEnum.CoinEnum.BTY.getType());
            assetDO.setBalance(BigDecimal.ZERO);
            assetDO.setFrozenBalance(BigDecimal.ZERO);
            list.add(assetDO);
        }

        List<MUserBackAssetVO> assetVOS = list.stream().map(o -> {
            MUserBackAssetVO assetVO = BeanCopierUtils.copy(o, MUserBackAssetVO.class);
            ChainPaymentProperties.Detail properties = chainPaymentProperties.getByCoinEnum(ChainEnum.CoinEnum.exist(assetVO.getCoinType()));
            if (properties == null) {
                assetVO.setChainBalance(BigDecimal.ZERO);
            } else {
                UserAdminDO adminDO = userAdminService.getByAddress(assetVO.getAddress());
                TxResult<BigDecimal> txResult = Web3jUtils.getBalance(properties.getChainUrl(), properties.getContractAddress(), adminDO.getInsideAddress(), properties.getDecimals());
                assetVO.setChainBalance(txResult.getResult());
            }
            return assetVO;
        }).toList();
        return PageVOUtils.pageVO(pageInfo, assetVOS);
    }

    @Operation(summary = "查询记录")
    @PostMapping("/log/page")
    public ResponseVO<PageVO<MUserBackAssetChangeHistoryVO>> logPage(@RequestBody MUserBackAssetLogPageQO pageQO) {
        if (ThreadInfo.getInfo().getRole() == UserEnum.RoleEnum.merchant.getRole()) {
            pageQO.setAddress(ThreadInfo.getInfo().getParentAddress());
        }

        PageInfo<UserBackAssetChangeHistoryDO> pageInfo = userBackAssetService.pageChangeHistory(pageQO);
        return PageVOUtils.pageVO(pageInfo, MUserBackAssetChangeHistoryVO.class);
    }

    @Operation(summary = "提现")
    @PostMapping("/withdraw/apply")
    public ResponseVO<Object> withdraw(@RequestBody MCoinNumberRO coinNumberRO) {
        AssertUtils.isNotNull(coinNumberRO.getNumber(), ResponseEnum.invalid_parameter);

        WithdrawProperties.Withdraw withdraw = withdrawProperties.getByCoinEnum(ChainEnum.CoinEnum.exist(coinNumberRO.getCoinType()));
        AssertUtils.isNotNull(withdraw, ResponseEnum.invalid_parameter);

        AssertUtils.isTrue(withdraw.getEnable(), ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(coinNumberRO.getNumber().compareTo(withdraw.getMinNumber()) >= 0, ResponseEnum.invalid_parameter);

        userBackAssetService.withdraw(ThreadInfo.getInfo().getParentAddress(), coinNumberRO.getCoinType(), coinNumberRO.getNumber());

        return ResponseUtils.success();
    }

}
