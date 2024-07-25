package com.vv;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Update;
import com.vv.controllers.UpdateController;
import com.vv.ton.DepositChecker;
import org.ton.java.address.Address;
import org.ton.java.tonlib.Tonlib;

import java.util.ArrayList;

public class Main {
    public static TelegramBot bot;
    public static final boolean IS_DEBUG = false;
    public static final Long CHANNEL_ID = IS_DEBUG ? -1001840101787L : -1002085046798L;
    public static final String ADDRESS_OF_INVEST_CONTRACT = IS_DEBUG ? "EQC5O9RnkY80GohbmKgE2WKUc7e9T8GyMGSObZKGc_O8xkrS" : "EQC5O9RnkY80GohbmKgE2WKUc7e9T8GyMGSObZKGc_O8xkrS";//"EQAgLlmQbbvyCnXps1mvgmMjUH-IoSU0WLaKye0BhrkjgLDU";//"EQACyLOE5U2o_raTdrXscCeC3oSX0zNcfZufoCUe1HOlbP8i"; //"";//"EQAflvsHsM0JT30rkErydaV-1ACKm2sfTASBmyb2ojsYbm3c"; //;
    public static Tonlib tonlib;
    private static final ArrayList<Long> admins = new ArrayList<>();
    public static final String BOT_REF_URL = "https://t.me/farmingprotocolton_bot?start=ref-";
    public static final String CHANNEL_URL = IS_DEBUG ? "https://t.me/+t0WDg86jwxYzYzVk" : "https://t.me/farmingprotocolton";

    public static void main(String[] args) {
        DepositChecker.init();
        tonlib = Tonlib.builder().testnet(false).pathToTonlibSharedLib(IS_DEBUG ? "/home/tonlibjson.dll" : "/home/tonlibjson-linux-x86_64.so").ignoreCache(false).build();
        try {
            tonlib.getSeqno(Address.of(ADDRESS_OF_INVEST_CONTRACT));
        } catch (Exception | Error e) {
            e.printStackTrace();
        }
        bot = new TelegramBot(IS_DEBUG ? "6632498327:AAHx1k-cH3zYjBdimtCHobOQrjSI7pKw8qs" : "6885867146:AAH9Ls_vYmStmO9fbrpNphJmfXuWdn6X-qU");//"");
        bot.setUpdatesListener((updates) -> {
            for (Update update : updates) {
                try {
                    System.out.println(update);
                    if (update.message() != null && update.message().chat().type() == Chat.Type.Private) {
                        new Thread(() -> UpdateController.process(update)).start();
                    } else if (update.message() == null && update.channelPost() == null) {
                        new Thread(() -> UpdateController.process(update)).start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    public static TelegramBot getBot() {
        return bot;
    }

    public static boolean isAdmin(Long chatId) {
        return admins.contains(chatId);
    }
}
