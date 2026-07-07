package com.fzm.mall.configuration;

import com.fzm.mall.constant.ChainConstant;
import com.fzm.mall.entity.dataobject.ChainContractDO;
import com.fzm.mall.entity.properties.ContractProperties;
import com.fzm.mall.mapper.ChainContractMapper;
import com.fzm.mall.third.chain.contract.ERC1155Manager;
import com.fzm.mall.third.chain.entity.TxResult;
import com.fzm.mall.third.chain.entity.TxResultEnum;
import com.fzm.mall.third.component.FileComponent;
import com.fzm.mall.util.AssertUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicationInitRunner implements ApplicationRunner {
    private final ChainContractMapper chainContractMapper;

    private final ContractProperties contractProperties;
    private final FileComponent fileComponent;

    @Override
    public void run(ApplicationArguments args) {
        ChainContractDO contractDO = chainContractMapper.getByCtId(ChainConstant.contract_default_contract_id);
        if (contractDO != null) {
            return;
        }

        TxResult<String> deployTr = ERC1155Manager.init(contractProperties.getChainUrl(), contractProperties.getChainId()).deploy(contractProperties.getDeployTempPrivateKey(), fileComponent.getHost());
        AssertUtils.isTrue(deployTr.getStatus() == TxResultEnum.SUCCESS, "合约部署失败");

        if (StringUtils.isNotBlank(contractProperties.getDeployAdminAddress())) {
            TxResult<String> transferSuperAdmin = ERC1155Manager.init(contractProperties.getChainUrl(), contractProperties.getChainId(), deployTr.getResult()).transferSuperAdmin(contractProperties.getDeployTempPrivateKey(), contractProperties.getDeployAdminAddress());
            AssertUtils.isTrue(transferSuperAdmin.getStatus() == TxResultEnum.SUCCESS, "合约转移失败");
        }

        int i = chainContractMapper.insert(ChainConstant.contract_default_contract_id, "ERC1155", "Default Contract", deployTr.getResult());
        AssertUtils.isTrue(i == 1, "合约部署失败");
    }
}
