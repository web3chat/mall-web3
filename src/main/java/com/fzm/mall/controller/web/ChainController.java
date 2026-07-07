package com.fzm.mall.controller.web;

import com.fzm.mall.annotation.UnAuthorization;
import com.fzm.mall.component.ChainEventComponent;
import com.fzm.mall.constant.enums.GoodsEnum;
import com.fzm.mall.constant.response.ResponseEnum;
import com.fzm.mall.constant.response.ResponseUtils;
import com.fzm.mall.constant.response.ResponseVO;
import com.fzm.mall.entity.dataobject.ChainContractDO;
import com.fzm.mall.entity.dataobject.GoodsSkuDO;
import com.fzm.mall.entity.dataobject.OrderBlindBoxDO;
import com.fzm.mall.entity.properties.ContractProperties;
import com.fzm.mall.entity.requestobject.ContractTxHashSubmitRO;
import com.fzm.mall.entity.viewobject.ChainContractVO;
import com.fzm.mall.entity.viewobject.TokenMetaVO;
import com.fzm.mall.service.ChainService;
import com.fzm.mall.service.GoodsService;
import com.fzm.mall.service.OrderService;
import com.fzm.mall.third.chain.entity.TxResult;
import com.fzm.mall.third.chain.entity.TxResultEnum;
import com.fzm.mall.third.chain.util.Web3jUtils;
import com.fzm.mall.util.AssertUtils;
import com.fzm.mall.util.BeanCopierUtils;
import com.fzm.mall.util.FormatVOUtils;
import com.fzm.mall.util.ParamsUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.util.List;

@Tag(name = "区块链")
@RestController
@UnAuthorization
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(value = "/v/chain", produces = MediaType.APPLICATION_JSON_VALUE)
public class ChainController {
    private final ChainService chainService;
    private final OrderService orderService;
    private final GoodsService goodsService;
    private final ContractProperties contractProperties;
    private final ChainEventComponent chainEventComponent;

    @Operation(summary = "合约列表")
    @GetMapping("/contract/list")
    public ResponseVO<List<ChainContractVO>> listContract() {
        List<ChainContractDO> dos = chainService.listContract();
        List<ChainContractVO> vos = BeanCopierUtils.copyList(dos, ChainContractVO.class);
        return ResponseUtils.success(vos);
    }

    @Operation(summary = "提交哈希")
    @PostMapping("/tx-hash")
    public ResponseVO<TokenMetaVO> txHash(@RequestBody ContractTxHashSubmitRO submitRO) {
        ParamsUtils.positiveInteger(submitRO.getCtId());
        String txHash = ParamsUtils.txHash(submitRO.getTxHash());

        ChainContractDO contractDO = chainService.getContractByCtId(submitRO.getCtId());
        AssertUtils.isNotNull(contractDO, ResponseEnum.invalid_parameter);

        TxResult<TransactionReceipt> tr = Web3jUtils.ethGetTransactionReceipt(contractProperties.getChainUrl(), txHash);
        AssertUtils.isTrue(tr.getStatus() == TxResultEnum.SUCCESS, tr.getError());

        chainEventComponent.handleLogs(tr.getResult().getLogs(), contractDO);

        TokenMetaVO metaVO = null;
        if (submitRO.getRespType() != null) {
            if (submitRO.getRespType() == GoodsEnum.SpuTypeEnum.blind_box.getType()) {
                OrderBlindBoxDO boxDO = orderService.getBlindBoxByTxHash(txHash);
                if (boxDO != null) {
                    GoodsSkuDO skuDO = goodsService.getSkuBySkuId(boxDO.getRewardSkuId());
                    metaVO = FormatVOUtils.getTokenMetaVO(skuDO, boxDO.getTokenId());
                }
            }
        }
        return ResponseUtils.success(metaVO);
    }
}
