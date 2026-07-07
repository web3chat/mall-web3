package com.fzm.mall.third.chain.contract;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the
 * <a href="https://github.com/hyperledger-web3j/web3j/tree/main/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 1.6.2.
 */

public class ERC20API extends Contract {
    public static final String BINARY = "608060405234801561001057600080fd5b50610448806100206000396000f3fe608060405234801561001057600080fd5b50600436106100625760003560e01c806318160ddd1461006757806323b872dd14610085578063313ce567146100b557806370a08231146100d357806395d89b4114610103578063a9059cbb14610121575b600080fd5b61006f610151565b60405161007c91906102f9565b60405180910390f35b61009f600480360381019061009a91906101cb565b610156565b6040516100ac91906102bc565b60405180910390f35b6100bd61015f565b6040516100ca9190610314565b60405180910390f35b6100ed60048036038101906100e891906101a2565b610164565b6040516100fa91906102f9565b60405180910390f35b61010b61016b565b60405161011891906102d7565b60405180910390f35b61013b6004803603810190610136919061021a565b610170565b60405161014891906102bc565b60405180910390f35b600090565b60009392505050565b600090565b6000919050565b606090565b600092915050565b600081359050610187816103e4565b92915050565b60008135905061019c816103fb565b92915050565b6000602082840312156101b457600080fd5b60006101c284828501610178565b91505092915050565b6000806000606084860312156101e057600080fd5b60006101ee86828701610178565b93505060206101ff86828701610178565b92505060406102108682870161018d565b9150509250925092565b6000806040838503121561022d57600080fd5b600061023b85828601610178565b925050602061024c8582860161018d565b9150509250929050565b61025f8161035d565b82525050565b60006102708261032f565b61027a818561033a565b935061028a8185602086016103a0565b610293816103d3565b840191505092915050565b6102a781610389565b82525050565b6102b681610393565b82525050565b60006020820190506102d16000830184610256565b92915050565b600060208201905081810360008301526102f18184610265565b905092915050565b600060208201905061030e600083018461029e565b92915050565b600060208201905061032960008301846102ad565b92915050565b600081519050919050565b600082825260208201905092915050565b600061035682610369565b9050919050565b60008115159050919050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6000819050919050565b600060ff82169050919050565b60005b838110156103be5780820151818401526020810190506103a3565b838111156103cd576000848401525b50505050565b6000601f19601f8301169050919050565b6103ed8161034b565b81146103f857600080fd5b50565b61040481610389565b811461040f57600080fd5b5056fea264697066735822122055c3712f74e5038065604ab99d5b3ff020f2955d0fe9d65bd6f0b71e951b2adb64736f6c63430008010033";

    public static final String FUNC_BALANCEOF = "balanceOf";

    public static final String FUNC_DECIMALS = "decimals";

    public static final String FUNC_SYMBOL = "symbol";

    public static final String FUNC_TOTALSUPPLY = "totalSupply";

    public static final String FUNC_TRANSFER = "transfer";

    public static final String FUNC_TRANSFERFROM = "transferFrom";

    public static final Event TRANSFER_EVENT = new Event("Transfer",
            List.of(new TypeReference<Address>(true) {
            }, new TypeReference<Address>(true) {
            }, new TypeReference<Uint256>() {
            }));
    private static final Credentials credentials0x0 = Credentials.create("0x0");
    private static final DefaultGasProvider defaultGasProvider = new DefaultGasProvider();

    protected ERC20API(String contractAddress, Web3j web3j, Credentials credentials,
                       ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    protected ERC20API(String contractAddress, Web3j web3j, TransactionManager transactionManager,
                       ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<TransferEventResponse> getTransferEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(TRANSFER_EVENT, transactionReceipt);
        ArrayList<TransferEventResponse> responses = new ArrayList<>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            TransferEventResponse typedResponse = new TransferEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static TransferEventResponse getTransferEventFromLog(Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(TRANSFER_EVENT, log);
        TransferEventResponse typedResponse = new TransferEventResponse();
        typedResponse.log = log;
        typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public static ERC20API load(String contractAddress, Web3j web3j,
                                TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new ERC20API(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static ERC20API load(String contractAddress, Web3j web3j) {
        return new ERC20API(contractAddress, web3j, credentials0x0, defaultGasProvider);
    }

    public RemoteFunctionCall<BigInteger> balanceOf(String account) {
        final Function function = new Function(FUNC_BALANCEOF,
                List.of(new Address(160, account)),
                List.of(new TypeReference<Uint256>() {
                }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<BigInteger> decimals() {
        final Function function = new Function(FUNC_DECIMALS,
                List.of(),
                List.of(new TypeReference<Uint8>() {
                }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<String> symbol() {
        final Function function = new Function(FUNC_SYMBOL,
                List.of(),
                List.of(new TypeReference<Utf8String>() {
                }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<BigInteger> totalSupply() {
        final Function function = new Function(FUNC_TOTALSUPPLY,
                List.of(),
                List.of(new TypeReference<Uint256>() {
                }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> transfer(String to, BigInteger value) {
        final Function function = new Function(
                FUNC_TRANSFER,
                List.of(new Address(160, to), new Uint256(value)),
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> transferFrom(String from, String to,
                                                               BigInteger value) {
        final Function function = new Function(
                FUNC_TRANSFERFROM,
                List.of(new Address(160, from),
                        new Address(160, to),
                        new Uint256(value)),
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }

    public static class TransferEventResponse extends BaseEventResponse {
        public String from;
        public String to;

        public BigInteger value;
    }
}
