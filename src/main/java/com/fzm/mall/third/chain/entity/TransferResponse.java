package com.fzm.mall.third.chain.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {
    public String from;
    public String to;
    public BigInteger value;
}
