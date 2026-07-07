package com.fzm.mall.third.chain.util;

import com.fzm.mall.third.chain.contract.ERC20API;
import com.fzm.mall.third.chain.entity.TxResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

@Slf4j
public class Web3jUtils {

    public static TxResult<BigInteger> ethBlockNumber(String url) {
        try (Web3j web3j = Web3j.build(new HttpService(url))) {
            EthBlockNumber sendResponse = web3j.ethBlockNumber().send();
            if (sendResponse.hasError()) {
                return TxResult.failure(sendResponse.getError().getMessage());
            }
            return TxResult.success(sendResponse.getBlockNumber());
        } catch (Exception e) {
            log.warn("Web3j eth_blockNumber error. {} {}", url, e.getMessage());
            return TxResult.failure(e.getMessage());
        }
    }

    public static TxResult<BigDecimal> getBalance(String url, String contractAddress, String address, Integer decimals) {
        try (Web3j web3j = Web3j.build(new HttpService(url))) {
            if (StringUtils.isBlank(contractAddress)) {
                EthGetBalance sendResponse = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
                if (sendResponse.hasError()) {
                    return TxResult.failure(sendResponse.getError().getMessage(), BigDecimal.ZERO);
                }
                return TxResult.success(new BigDecimal(sendResponse.getBalance()).movePointLeft(decimals).setScale(decimals, RoundingMode.DOWN));
            } else {
                ERC20API erc20API = ERC20API.load(contractAddress, web3j);
                BigInteger balance = erc20API.balanceOf(address).send();
                return TxResult.success(new BigDecimal(balance).movePointLeft(decimals).setScale(decimals, RoundingMode.DOWN));
            }
        } catch (Exception e) {
            log.warn("Web3j eth_getBalance error. {} {}", url, e.getMessage());
            return TxResult.failure(e.getMessage(), BigDecimal.ZERO);
        }
    }

    public static TxResult<TransactionReceipt> ethGetTransactionReceipt(String url, String hash) {
        try (Web3j web3j = Web3j.build(new HttpService(url))) {
            EthGetTransactionReceipt sendResponse = web3j.ethGetTransactionReceipt(hash).send();
            if (sendResponse.hasError()) {
                return TxResult.failure(sendResponse.getError().getMessage());
            }
            if (sendResponse.getTransactionReceipt().isEmpty()) {
                return TxResult.running();
            }
            TransactionReceipt transactionReceipt = sendResponse.getTransactionReceipt().get();
            if (transactionReceipt.isStatusOK()) {
                return TxResult.success(transactionReceipt);
            }
            return TxResult.failure(transactionReceipt.getRevertReason());
        } catch (Exception e) {
            log.warn("Web3j eth_getTransactionReceipt error. {} {}", url, e.getMessage());
            return TxResult.failure(e.getMessage());
        }
    }

    public static TxResult<Transaction> ethGetTransactionByHash(String url, String hash) {
        try (Web3j web3j = Web3j.build(new HttpService(url))) {
            EthTransaction sendResponse = web3j.ethGetTransactionByHash(hash).send();
            if (sendResponse.hasError()) {
                return TxResult.failure(sendResponse.getError().getMessage());
            }
            return TxResult.success(sendResponse.getTransaction().orElseThrow());
        } catch (Exception e) {
            log.warn("Web3j eth_getTransactionByHash error. {} {}", url, e.getMessage());
            return TxResult.failure(e.getMessage());
        }
    }


    public static TxResult<EthLog> ethGetLogs(String url, org.web3j.protocol.core.methods.request.EthFilter filter) {
        try (Web3j web3j = Web3j.build(new HttpService(url))) {
            EthLog ethLog = web3j.ethGetLogs(filter).send();
            if (ethLog.hasError()) {
                return TxResult.failure(ethLog.getError().getMessage());
            }
            return TxResult.success(ethLog);
        } catch (Exception e) {
            log.warn("Web3j eth_getLogs error. {} {}", url, e.getMessage());
            return TxResult.failure(e.getMessage());
        }
    }

    public static TxResult<EthBlock.Block> ethGetBlockByNumber(String url, BigInteger blockNumber, boolean returnFullTransactionObjects) {
        try (Web3j web3j = Web3j.build(new HttpService(url))) {
            EthBlock ethBlock = web3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), returnFullTransactionObjects).send();
            if (ethBlock.hasError()) {
                return TxResult.failure(ethBlock.getError().getMessage());
            }
            return TxResult.success(ethBlock.getBlock());
        } catch (Exception e) {
            log.warn("Web3j eth_getBlockByNumber error. {} {}", url, e.getMessage());
            return TxResult.failure(e.getMessage());
        }
    }

    public static TxResult<String> transfer(String url, Long chainId, String contractAddress, String fromPrivateKey, String toAddress, BigDecimal number, Integer decimals) {
        try (Web3j web3j = Web3j.build(new HttpService(url))) {
            BigInteger valueNumber = number.movePointRight(decimals).toBigInteger();

            if (chainId == null || chainId <= 0) {
                chainId = web3j.ethChainId().send().getChainId().longValueExact();
            }

            RawTransactionManager rawTransactionManager = new RawTransactionManager(web3j, Credentials.create(fromPrivateKey), chainId);

            BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();

            // 合约地址为空，表示主代币
            if (StringUtils.isBlank(contractAddress)) {
                // 主代币目前这个数量比较合适
                BigInteger gasLimit = BigInteger.valueOf(21000);

                EthSendTransaction ethSendTransaction = rawTransactionManager.sendTransaction(gasPrice, gasLimit, toAddress, "", valueNumber);
                if (ethSendTransaction == null) {
                    return TxResult.failure("failure");
                }
                if (ethSendTransaction.hasError()) {
                    return TxResult.failure(ethSendTransaction.getError().getMessage());
                } else {
                    return TxResult.success(ethSendTransaction.getTransactionHash());
                }
            }
            // 合约地址为空，表示合约代币
            else {
                // 不同的合约代币消耗量不同，这个数量对绝大多数合约都比较合适
                BigInteger gasLimit = BigInteger.valueOf(100000);

                ERC20API erc20API = ERC20API.load(contractAddress, web3j, rawTransactionManager, new StaticGasProvider(gasPrice, gasLimit));
                TransactionReceipt receipt = erc20API.transfer(toAddress, valueNumber).send();

                if (receipt.isStatusOK()) {
                    return TxResult.success(receipt.getTransactionHash());
                } else {
                    return TxResult.failure(receipt.getRevertReason());
                }
            }

        } catch (Exception e) {
            log.warn("Web3j transfer error. {} {}", url, e.getMessage());
            return TxResult.failure(e.getMessage());
        }
    }
}
