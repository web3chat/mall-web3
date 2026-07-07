package com.fzm.mall.third.chain.wallet;

import com.fzm.mall.entity.properties.ContractProperties;
import com.fzm.mall.util.SpringUtils;
import org.web3j.crypto.MnemonicUtils;

public class WalletSeed {
    protected static final byte[] ethSeed;
    private static final ContractProperties CONTRACT_PROPERTIES = SpringUtils.getBean(ContractProperties.class);

    static {
        ethSeed = MnemonicUtils.generateSeed(CONTRACT_PROPERTIES.getMnemonic(), CONTRACT_PROPERTIES.getWalletPassword());
    }
}
