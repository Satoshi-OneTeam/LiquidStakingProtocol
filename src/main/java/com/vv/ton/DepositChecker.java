package com.vv.ton;

import com.iwebpp.crypto.TweetNaclFast;
import com.vv.Main;
import com.vv.MongoBase;
import com.vv.models.User;
import org.ton.java.address.Address;
import org.ton.java.bitstring.BitString;
import org.ton.java.cell.Cell;
import org.ton.java.mnemonic.Mnemonic;
import org.ton.java.mnemonic.Pair;
import org.ton.java.smartcontract.types.ExternalMessage;
import org.ton.java.smartcontract.wallet.Options;
import org.ton.java.smartcontract.wallet.v3.WalletV3ContractR2;
import org.ton.java.smartcontract.wallet.v4.WalletV4ContractR2;
import org.ton.java.tonlib.Tonlib;
import org.ton.java.tonlib.types.RunResult;
import org.ton.java.tonlib.types.TvmStackEntryNumber;
import org.ton.java.utils.Utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static org.ton.java.cell.CellBuilder.beginCell;

public class DepositChecker {
    public static Pair keyPair;

    public static void init() {
        try {
            keyPair = Mnemonic.toKeyPair(List.of("bone", "burger", "valve", "taxi", "allow", "banana", "attend",
                    "traffic", "deny", "tomato", "label", "hand", "battle", "gloom", "stomach", "rescue", "glass", "image",
                    "horn", "drill", "brisk", "tennis", "loud", "because"));
        } catch (Exception | Error e) {
            e.printStackTrace();
        }
    }
    public static DepositInfo getDeposit(String wallet, User user) {
        try {

            Address address = Address.of(wallet);
            Address contract = Address.of(Main.ADDRESS_OF_INVEST_CONTRACT);
            Deque<String> stack = new ArrayDeque();
            stack.offer("[num, " + address.toDecimal() + "]");
            RunResult result = Main.tonlib.runMethod(contract, "deposit_info", stack);
            System.out.println(result);
            if (result.getExit_code() == 0) {
                BigInteger balance = ((TvmStackEntryNumber) result.getStack().get(0)).getNumber();
                BigInteger time = ((TvmStackEntryNumber) result.getStack().get(1)).getNumber();
                BigInteger refInfo = ((TvmStackEntryNumber) result.getStack().get(2)).getNumber();
                BigInteger refCode = ((TvmStackEntryNumber) result.getStack().get(3)).getNumber();
                return new DepositInfo(balance, time, refInfo, refCode);
            } else
                return new DepositInfo(new BigInteger("0"), new BigInteger("0"), new BigInteger("0"), new BigInteger("0"));
        } catch (Exception | Error e) {
            e.printStackTrace();
            return new DepositInfo(new BigInteger("0"), new BigInteger("0"), new BigInteger("0"), new BigInteger("0"));
        }
    }

    public static BigInteger getEstimatedProfit(String wallet) {
        try {
            Address address = Address.of(wallet);
            Address contract = Address.of(Main.ADDRESS_OF_INVEST_CONTRACT);
            Deque<String> stack = new ArrayDeque();
            stack.offer("[num, " + address.toDecimal() + "]");
            RunResult result = Main.tonlib.runMethod(contract, "estimated_profit", stack);
            System.out.println(result);
            if (result.getExit_code() == 0) {
                BigInteger balance = ((TvmStackEntryNumber) result.getStack().get(0)).getNumber();
                return balance;
            } else
                return new BigInteger("0");
        } catch (Exception | Error e) {
            e.printStackTrace();
            return new BigInteger("0");
        }
    }

    public static void lel(Tonlib tonlib, String refCode, Address addressContract) {
        Deque<String> stack = new ArrayDeque();
        BitString bitString = new BitString(1023);
        bitString.writeString(refCode);
        BigInteger bigInteger = bitString.readUint(48);
        System.out.println(bigInteger);
        stack.offer("[num, " + bigInteger.toString(10) + "]");
        RunResult result = tonlib.runMethod(addressContract, "ref_info", stack);
        BigInteger balance = ((TvmStackEntryNumber) result.getStack().get(0)).getNumber();
        System.out.println(result);
        System.out.println(balance);
        String lel = new String(balance.toByteArray());
        System.out.println(lel);
    }

    public static DepositInfo getDeposit(String wallet, Tonlib tonlib, String addressContract) {
        try {

            Address address = Address.of(wallet);
            Address contract = Address.of(addressContract);
            Deque<String> stack = new ArrayDeque();
            stack.offer("[num, " + address.toDecimal() + "]");
            RunResult result = tonlib.runMethod(contract, "deposit_info", stack);
            System.out.println(result);
            if (result.getExit_code() == 0) {
                BigInteger balance = ((TvmStackEntryNumber) result.getStack().get(0)).getNumber();
                BigInteger time = ((TvmStackEntryNumber) result.getStack().get(1)).getNumber();
                BigInteger refInfo = ((TvmStackEntryNumber) result.getStack().get(2)).getNumber();
                BigInteger refCode = ((TvmStackEntryNumber) result.getStack().get(3)).getNumber();
                return new DepositInfo(balance, time, refInfo, refCode);
            } else
                return new DepositInfo(new BigInteger("0"), new BigInteger("0"), new BigInteger("0"), new BigInteger("0"));
        } catch (Exception | Error e) {
            e.printStackTrace();
            return new DepositInfo(new BigInteger("0"), new BigInteger("0"), new BigInteger("0"), new BigInteger("0"));
        }
    }

    public static void sendMessage(String code, String walletAddress) {

        Options options1 = Options.builder()
                .publicKey(keyPair.getPublicKey())
                .wc(0L)
                .build();

        WalletV4ContractR2 wallet = new WalletV4ContractR2(options1);
        System.out.println(wallet.getAddress().toString(true, true, true, false));
        long seqno = wallet.getSeqno(Main.tonlib);
        BitString bitString = new BitString(1023);
        bitString.writeString(code);
        BigInteger bigInteger = bitString.readUint(48);
        Cell cell = beginCell().storeUint(1, 32).storeUint(Address.of(walletAddress).toDecimal(), 256).storeUint(bigInteger, 48).endCell();
        ExternalMessage extMsg = wallet.createTransferMessage(
                keyPair.getSecretKey(),
                Address.of(Main.ADDRESS_OF_INVEST_CONTRACT),
                Utils.toNano("0.1"),
                seqno,
                cell
        );
        Main.tonlib.sendRawMessage(Utils.bytesToBase64(extMsg.message.toBoc(false)));
    }
}
