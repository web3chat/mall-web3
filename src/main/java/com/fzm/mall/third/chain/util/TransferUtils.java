package com.fzm.mall.third.chain.util;

import com.fzm.mall.constant.response.ResponseEnum;
import com.fzm.mall.constant.response.ValidateException;
import com.fzm.mall.entity.properties.ChainPaymentProperties;
import com.fzm.mall.third.chain.contract.FzmERC1155;
import com.fzm.mall.third.chain.entity.TransferResponse;
import com.fzm.mall.third.chain.entity.TxResult;
import com.fzm.mall.third.chain.entity.TxResultEnum;
import com.fzm.mall.util.AssertUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TransferUtils {

    public static PayResult payment(ChainPaymentProperties.Detail detail, String txHash, String verifyFromAddress, String verifyToAddress, BigDecimal verifyAmount) {
        return payment(detail.getChainUrl(), detail.getContractAddress(), txHash, verifyFromAddress, verifyToAddress, verifyAmount, detail.getDecimals());
    }

    private static PayResult payment(String chainUrl, String contractAddress, String hash, String fromAddress, String toAddress, BigDecimal amount, Integer decimals) {
        // 先检查交易状态
        TxResult<TransactionReceipt> trReceipt = Web3jUtils.ethGetTransactionReceipt(chainUrl, hash);
        if (trReceipt.getStatus() != TxResultEnum.SUCCESS) {
            throw new ValidateException(ResponseEnum.Invalid_transaction_hash);
        }

        // BTY链有2个哈希，只有一个是真实的
        String queryHash = trReceipt.getResult().getTransactionHash().toLowerCase();
        if (!queryHash.equals(hash)) {
            return payment(chainUrl, contractAddress, queryHash, fromAddress, toAddress, amount, decimals);
        }

        // 检查交易详情
        TxResult<Transaction> trTransaction = Web3jUtils.ethGetTransactionByHash(chainUrl, hash);
        if (trTransaction.getStatus() != TxResultEnum.SUCCESS) {
            throw new ValidateException(ResponseEnum.Invalid_transaction_hash);
        }

        Transaction transaction = trTransaction.getResult();

        BigDecimal transferAmount;

        // 合约为空，主代币
        if (StringUtils.isBlank(contractAddress)) {
            AssertUtils.isTrue(fromAddress.equalsIgnoreCase(transaction.getFrom()), ResponseEnum.Invalid_transaction_hash);
            AssertUtils.isTrue(toAddress.equalsIgnoreCase(transaction.getTo()), ResponseEnum.Invalid_transaction_hash);

            transferAmount = new BigDecimal(transaction.getValue()).movePointLeft(decimals);
        }
        // 合约不为空
        else {
            AssertUtils.isTrue(contractAddress.equalsIgnoreCase(transaction.getTo()), ResponseEnum.Invalid_transaction_hash);

            try {
                TransferResponse transferResponse = ParseUtils.getTransferResponseFromTransaction(transaction);

                AssertUtils.isTrue(fromAddress.equalsIgnoreCase(transferResponse.from), ResponseEnum.Invalid_transaction_hash);
                AssertUtils.isTrue(toAddress.equalsIgnoreCase(transferResponse.to), ResponseEnum.Invalid_transaction_hash);

                transferAmount = new BigDecimal(transferResponse.value).movePointLeft(decimals);
            } catch (ValidateException e) {
                throw e;
            } catch (Exception e) {
                throw new ValidateException(ResponseEnum.Invalid_transaction_hash);
            }
        }

        if (amount != null) {
            AssertUtils.isTrue(transferAmount.compareTo(amount) >= 0, ResponseEnum.insufficient_payment_amount);
        }

        return new PayResult(hash, transferAmount);
    }

    public static Erc1155Result erc1155(String chainUrl, String contractAddress, String hash, String fromAddress, String toAddress, List<Long> verifyTokenIds) {
        // 先检查交易状态
        TxResult<TransactionReceipt> trReceipt = Web3jUtils.ethGetTransactionReceipt(chainUrl, hash);
        if (trReceipt.getStatus() != TxResultEnum.SUCCESS) {
            throw new ValidateException(ResponseEnum.Invalid_transaction_hash);
        }
        // BTY链有2个哈希，只有一个是真实的
        String queryHash = trReceipt.getResult().getTransactionHash().toLowerCase();
        if (!queryHash.equals(hash)) {
            return erc1155(chainUrl, contractAddress, queryHash, fromAddress, toAddress, verifyTokenIds);
        }

        // 检查交易详情
        TxResult<Transaction> trTransaction = Web3jUtils.ethGetTransactionByHash(chainUrl, hash);
        if (trTransaction.getStatus() != TxResultEnum.SUCCESS) {
            throw new ValidateException(ResponseEnum.Invalid_transaction_hash);
        }
        AssertUtils.isTrue(contractAddress.equalsIgnoreCase(trTransaction.getResult().getTo()), ResponseEnum.Invalid_transaction_hash);

        Set<Long> tokenIds = new HashSet<>();

        for (Log ethLog : trReceipt.getResult().getLogs()) {
            // 单笔转账
            FzmERC1155.TransferSingleEventResponse singleEventResponse = FzmERC1155.getTransferSingleEventFromLog(ethLog);
            if (singleEventResponse != null) {
                if (fromAddress.equalsIgnoreCase(singleEventResponse.from) && toAddress.equalsIgnoreCase(singleEventResponse.to)) {
                    tokenIds.add(singleEventResponse.id.longValue());
                }
            }
            // 批量转账
            FzmERC1155.TransferBatchEventResponse batchEventResponse = FzmERC1155.getTransferBatchEventFromLog(ethLog);
            if (batchEventResponse != null) {
                if (fromAddress.equalsIgnoreCase(batchEventResponse.from) && toAddress.equalsIgnoreCase(batchEventResponse.to)) {
                    tokenIds.addAll(batchEventResponse.ids.stream().map(BigInteger::longValue).toList());
                }
            }
        }

        if (verifyTokenIds != null) {
            AssertUtils.isTrue(tokenIds.equals(new HashSet<>(verifyTokenIds)), ResponseEnum.Invalid_transaction_hash);
        }

        return new Erc1155Result(hash, tokenIds);
    }

    public record PayResult(String hash, BigDecimal amount) {
    }

    public record Erc1155Result(String hash, Set<Long> tokenIds) {
    }
}
