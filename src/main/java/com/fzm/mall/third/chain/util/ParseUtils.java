package com.fzm.mall.third.chain.util;

import com.fzm.mall.third.chain.entity.TransferResponse;
import org.apache.commons.lang3.StringUtils;
import org.web3j.protocol.core.methods.response.Transaction;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ParseUtils {
    private static final int func_length = 8;
    private static final int data_length = 64;
    private static final int address_prefix_zero_length = 24;

    private static final String method_id_transfer = "a9059cbb";
    private static final String method_id_transfer_from = "23b872dd";

    public static TransferResponse getTransferResponseFromTransaction(Transaction transaction) {
        return getTransferResponseFromTransaction(transaction.getInput(), transaction.getFrom());
    }

    public static TransferResponse getTransferResponseFromTransaction(String input, String from) {
        TransferResponse transferResponse = new TransferResponse();

        List<String> indexValue;
        try {
            indexValue = getIndexValueFromInput(input);
        } catch (Exception e) {
            return null;
        }

        // transfer(address to, uint256 value)
        // log(method, to, value)
        if (method_id_transfer.equals(indexValue.get(0))) {
            transferResponse.from = from;
            transferResponse.to = address(indexValue.get(1));
            transferResponse.value = bigInteger(indexValue.get(2));
        }
        // transferFrom(address from, address to, uint256 value)
        // log(method, from, to, value)
        else if (method_id_transfer_from.equals(indexValue.get(0))) {
            transferResponse.from = address(indexValue.get(1));
            transferResponse.to = address(indexValue.get(2));
            transferResponse.value = bigInteger(indexValue.get(3));
        }
        // error
        else {
            return null;
        }

        return transferResponse;
    }

    private static String address(String str) {
        return "0x" + StringUtils.substring(str, address_prefix_zero_length);
    }

    private static BigInteger bigInteger(String str) {
        return new BigInteger(str, 16);
    }

    private static List<String> getIndexValueFromInput(String input) {
        input = input.startsWith("0x") ? StringUtils.substring(input, 2) : input;

        String funcId = StringUtils.substring(input, 0, func_length);
        input = StringUtils.substring(input, func_length);

        int size = input.length() / data_length;
        if (input.length() % data_length != 0) {
            size += 1;
        }

        List<String> list = new ArrayList<>(size + 1);
        list.add(funcId);

        for (int i = 0; i < size; i++) {
            String substr = StringUtils.substring(input, i * data_length, (i + 1) * data_length);
            list.add(substr);
        }
        return list;
    }
}
