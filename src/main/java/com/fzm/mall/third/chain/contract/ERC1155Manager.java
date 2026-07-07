package com.fzm.mall.third.chain.contract;

import com.fzm.mall.third.chain.entity.TxResult;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ERC1155Manager {
    private static final OkHttpClient client = new OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    private static final BigInteger def_gas_price = BigInteger.valueOf(1_000_000);
    private static final long def_gas_limit = 100_000L;
    private static final long gas_limit_fix = def_gas_limit * 5;
    private final Web3j web3j;
    private final Long chainId;
    private final String contractAddress;


    public ERC1155Manager(String chainUrl, Long chainId, String contractAddress) {
        this.web3j = Web3j.build(new HttpService(chainUrl, client));
        this.chainId = chainId;
        this.contractAddress = contractAddress;
    }

    public static ERC1155Manager init(String chainUrl, Long chainId) {
        return new ERC1155Manager(chainUrl, chainId, "");
    }

    public static ERC1155Manager init(String chainUrl, Long chainId, String contractAddress) {
        return new ERC1155Manager(chainUrl, chainId, contractAddress);
    }

    // ------------------------ 合约部署 ------------------------

    public static long dynamicMintGasLimit(long count) {
        return def_gas_limit * 2 + 30000L * count;
    }

    // ------------------------ 权限管理 ------------------------

    public static long dynamicTransferGasLimit(long count) {
        return def_gas_limit * 2 + 50000L * count;
    }

    // ------------------------ 代币管理 ------------------------

    /**
     * 部署ERC1155合约
     *
     * @param ownerPrivateKey 部署人私钥
     * @param baseURI         基础URI
     * @return TxResult<地址>
     */
    public TxResult<String> deploy(String ownerPrivateKey, String baseURI) {
        try {
            Credentials credentials = Credentials.create(ownerPrivateKey);
            RawTransactionManager txManager = new RawTransactionManager(web3j, credentials, chainId);

            FzmERC1155 contract = FzmERC1155.deploy(web3j, txManager, new DefaultGasProvider(), baseURI).send();
            log.info("合约部署成功，地址: {}", contract.getContractAddress());

            return TxResult.success(contract.getContractAddress());
        } catch (Exception e) {
            log.error("合约部署失败", e);
            return TxResult.failure("合约部署失败: " + e.getMessage());
        }
    }

    /**
     * 转移超级管理员权限
     * 仅当前超级管理员可调用，禁止授予商户角色，撤销当前超级管理员的管理员和超级管理员权限，添加新地址的管理员和超级管理员权限
     *
     * @param superAdminPrivateKey 超级管理员私钥
     * @param address              新超级管理员地址
     * @return TxResult<哈希>
     */
    public TxResult<String> transferSuperAdmin(String superAdminPrivateKey, String address) {
        try {
            FzmERC1155 contract = loadContract(superAdminPrivateKey, gas_limit_fix);

            TransactionReceipt receipt = contract.transferSuperAdmin(address).send();
            checkReceipt(receipt);

            log.info("转移超级管理员权限成功，哈希: {}", receipt.getTransactionHash());
            return TxResult.success(receipt.getTransactionHash());
        } catch (Exception e) {
            log.error("转移超级管理员权限失败", e);
            return TxResult.failure("转移超级管理员权限失败: " + e.getMessage());
        }
    }

    // ------------------------ 转账操作 ------------------------

    /**
     * 批量铸造NFT
     * 每个prefix对应独立铸造池，ID生成规则为：prefix * 1e8 + serial
     *
     * @param mintPrivateKey 铸造人私钥
     * @param prefix         铸造池编号（需<= type(uint256).max / 1e8）
     * @param start          起始序列号（含）
     * @param end            结束序列号（含）
     * @param canExpress     是否允许提货
     * @param isBlindBox     是否允许提货
     * @return TxResult<哈希>
     */
    public TxResult<String> mintBatchNFT(String toAddress, String mintPrivateKey, long prefix, long start, long end, boolean canExpress, boolean isBlindBox) {
        try {
            FzmERC1155 contract = loadContract(mintPrivateKey, dynamicMintGasLimit(end - start + 1));

            TransactionReceipt receipt = contract.mintBatchNFT(toAddress, BigInteger.valueOf(prefix), BigInteger.valueOf(start), BigInteger.valueOf(end), canExpress, isBlindBox).send();
            checkReceipt(receipt);

            log.info("批量铸造NFT成功，哈希: {}", receipt.getTransactionHash());
            return TxResult.success(receipt.getTransactionHash());
        } catch (Exception e) {
            log.error("批量铸造NFT失败", e);
            return TxResult.failure("批量铸造NFT失败: " + e.getMessage());
        }
    }

    // ------------------------ 视图函数 ------------------------

    /**
     * 修改前缀权限
     * 仅前缀铸造者可修改
     *
     * @param mintPrivateKey 铸造人私钥
     * @param prefixes       要修改的前缀数组
     * @param canExpresses   新的提货状态数组
     * @param isBlindBoxes   新的盲盒类型数组
     * @return TxResult<哈希>
     */
    public TxResult<String> updatePrefixMeta(String mintPrivateKey, List<Long> prefixes, List<Boolean> canExpresses, List<Boolean> isBlindBoxes) {
        try {
            FzmERC1155 contract = loadContract(mintPrivateKey, gas_limit_fix);

            List<BigInteger> list = prefixes.stream().map(BigInteger::valueOf).toList();

            TransactionReceipt receipt = contract.updatePrefixMeta(list, canExpresses, isBlindBoxes).send();
            checkReceipt(receipt);
            log.info("修改前缀权限成功，哈希: {}", receipt.getTransactionHash());

            return TxResult.success(receipt.getTransactionHash());
        } catch (Exception e) {
            log.error("修改前缀权限失败", e);
            return TxResult.failure("修改前缀权限失败: " + e.getMessage());
        }
    }

    /**
     * 多地址批量转账
     *
     * @param fromPrivateKey 发送地址私钥
     * @param multiTxs       转账数组
     * @return TxResult<哈希>
     */
    public TxResult<String> muxBatchTransferNFT(String fromPrivateKey, List<FzmERC1155.MultiTX> multiTxs) {
        try {
            int count = 0;
            for (FzmERC1155.MultiTX tx : multiTxs) {
                count += tx.ids.size();
            }

            FzmERC1155 contract = loadContract(fromPrivateKey, dynamicTransferGasLimit(count));

            TransactionReceipt receipt = contract.multiBatchTransferNFT(multiTxs).send();
            checkReceipt(receipt);
            log.info("多地址批量转账成功，哈希: {}", receipt.getTransactionHash());

            return TxResult.success(receipt.getTransactionHash());
        } catch (Exception e) {
            log.error("多地址批量转账失败", e);
            return TxResult.failure("多地址批量转账失败: " + e.getMessage());
        }
    }

    public TxResult<String> expressNFTByMerchant(String fromPrivateKey, String to, List<BigInteger> ids, String detail) {
        try {
            FzmERC1155 contract = loadContract(fromPrivateKey, gas_limit_fix);

            TransactionReceipt receipt = contract.expressNFTByMerchant(to, ids, detail).send();
            checkReceipt(receipt);
            log.info("购买并提货成功，哈希: {}", receipt.getTransactionHash());

            return TxResult.success(receipt.getTransactionHash());
        } catch (Exception e) {
            log.error("购买并提货成功失败", e);
            return TxResult.failure("购买并提货成功失败: " + e.getMessage());
        }
    }

    // ------------------------ 工具方法 ------------------------

    /**
     * 查询余额
     *
     * @param address 地址
     * @param tokenId 代币ID
     * @return TxResult<余额>
     */
    public TxResult<BigDecimal> balanceOf(String address, long tokenId) {
        // 无签名查询
        try {
            FzmERC1155 contract = loadContract(null, def_gas_limit);
            BigInteger balance = contract.balanceOf(address, BigInteger.valueOf(tokenId)).send();
            return TxResult.success(new BigDecimal(balance));
        } catch (Exception e) {
            return TxResult.failure("查询余额失败: " + e.getMessage(), BigDecimal.ZERO);
        }
    }

    /**
     * 查询基础元数据URI
     *
     * @param tokenId 代币ID
     * @return TxResult<URI>
     */
    public TxResult<String> uri(long tokenId) {
        // 无签名查询
        try {
            FzmERC1155 contract = loadContract(null, def_gas_limit);
            String uri = contract.uri(BigInteger.valueOf(tokenId)).send();
            return TxResult.success(uri);
        } catch (Exception e) {
            return TxResult.failure("查询URI失败: " + e.getMessage());
        }
    }

    private FzmERC1155 loadContract(String privateKey, long gasLimit) {
        Credentials credentials = StringUtils.isBlank(privateKey) ? Credentials.create("0x0") : Credentials.create(privateKey);
        RawTransactionManager txManager = new RawTransactionManager(web3j, credentials, chainId);

        return FzmERC1155.load(contractAddress, web3j, txManager, new StaticGasProvider(def_gas_price, BigInteger.valueOf(gasLimit)));
    }

    private void checkReceipt(TransactionReceipt receipt) {
        if (receipt == null || !receipt.isStatusOK()) {
            String errorMsg = "交易失败: " + (receipt != null ? receipt.getStatus() : "无回执");
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
    }
}
