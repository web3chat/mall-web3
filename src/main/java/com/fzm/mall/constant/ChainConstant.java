package com.fzm.mall.constant;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ChainConstant {

    public static final BigDecimal btyDivisor = BigDecimal.valueOf(Math.pow(10, 8));
    // 最大序列号(1e8)，不包含
    public static final long token_max_serial = btyDivisor.longValue();
    // 分批次发行，单次最大铸造数量
    public static final int token_mint_batch_max_num = 1000;
    // 多地址批量转账，单次最多地址数量
    public static final int multi_batch_transfer_max_address = 100;
    // 多地址批量转账，每个地址最多tokenId数量
    public static final int multi_batch_transfer_token_max_num_pre_address = 5;
    // 默认合约编号，ERC1155商品NFT发行的合约
    public static final int contract_default_contract_id = 1;

    public static BigDecimal btyDiv(long amount) {
        return BigDecimal.valueOf(amount).divide(ChainConstant.btyDivisor, 8, RoundingMode.DOWN);
    }
}
