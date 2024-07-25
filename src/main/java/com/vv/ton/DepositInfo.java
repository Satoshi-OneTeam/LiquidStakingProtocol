package com.vv.ton;

import org.ton.java.utils.Utils;

import java.math.BigDecimal;
import java.math.BigInteger;

public class DepositInfo {
    private BigInteger balance, time, refInfo, refCode;

    public DepositInfo(BigInteger balance, BigInteger time, BigInteger refInfo, BigInteger refCode) {
        this.balance = balance;
        this.time = time;
        this.refInfo = refInfo;
        this.refCode = refCode;
    }

    public BigDecimal getBalance() {
        return Utils.fromNano(this.balance.longValue());
    }

    public BigInteger getTime() {
        return time;
    }

    public BigInteger getRefInfo() {
        return refInfo;
    }

    public BigInteger getRefCode() {
        return refCode;
    }

    @Override
    public String toString() {
        return "DepositInfo{" +
                "balance=" + balance +
                ", time=" + time +
                ", refInfo=" + refInfo.toString(10) +
                ", refCode=" + refCode.toString(10) +
                '}';
    }
}