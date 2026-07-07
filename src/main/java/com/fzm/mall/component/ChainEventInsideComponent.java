package com.fzm.mall.component;

import com.fzm.mall.entity.dataobject.ChainContractDO;
import com.fzm.mall.entity.properties.ContractProperties;
import com.fzm.mall.third.chain.entity.TxResult;
import com.fzm.mall.third.chain.entity.TxResultEnum;
import com.fzm.mall.third.chain.util.Web3jUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ChainEventInsideComponent {
    private final ChainEventComponent chainEventComponent;
    private final ContractProperties contractProperties;

    @Async("chainEventInsideExecutor")
    public void handleEvent(ChainContractDO contractDO, String txHash) {
        if (StringUtils.isBlank(txHash) || contractDO == null) {
            return;
        }

        TxResult<TransactionReceipt> tr = Web3jUtils.ethGetTransactionReceipt(contractProperties.getChainUrl(), txHash);
        if (tr.getStatus() != TxResultEnum.SUCCESS) {
            return;
        }

        chainEventComponent.handleLogs(tr.getResult().getLogs(), contractDO);
    }
}
