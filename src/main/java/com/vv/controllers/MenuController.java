package com.vv.controllers;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import com.vv.Main;
import com.vv.MongoBase;
import com.vv.models.Status;
import com.vv.models.User;
import com.vv.ton.DepositChecker;
import com.vv.ton.DepositInfo;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.ton.java.utils.Utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MenuController {
    private static ArrayList<String> approvedCommands = new ArrayList<>();

    static {
        approvedCommands.add("/start");
    }

    public static boolean isMenuCommand(String command) {
        System.out.println(command);
        if(command.contains("/start")) return true;
        return approvedCommands.contains(command);
    }

    public static void process(Update update) {
        Long chatId = update.message().from().id();

        String message = update.message().text();

        String messageToSend = null;
        InlineKeyboardMarkup keyboard =  null;
        String subText = null;
        if (message.contains("/start") || message.contains("/referral")) {
            String[] args = message.split(" ");
            if (args.length == 2) {
                subText = args[1];
                message = args[0];
            }
        }
        System.out.println("DS");
        System.out.println(subText);
        if (message.contains("/start")) {
            User user;
            if(subText != null && subText.contains("ref")) {
                user = MongoBase.getInstance().getUser(chatId, subText.split("-")[1]);
            } else user = MongoBase.getInstance().getUser(chatId);

            StringBuilder sb = new StringBuilder();
            sb.append("<b>Добро пожаловать!</b>");
            if (user.getWallet().isBlank()) {
                sb.append("\n\nДля продолжения работы с ботом введите свой TON кошелёк.");
                StatusController.statuses.put(chatId, Status.ADDRESS);
            } else {
                sb.append("\n\nЛичная информация пользователя в Farming Protocol TON");
                sb.append("\n\n\uD83D\uDC8E ").append(user.getWallet());
                String addedText = !user.getInvitedByCode().isBlank() ? " с комментарием <code>" + user.getInvitedByCode() + "</code>," : "";
                DepositInfo depositInfo = DepositChecker.getDeposit(user.getWallet(), user);
                System.out.println(depositInfo);
                if (depositInfo.getBalance().compareTo(BigDecimal.ZERO) == 0 || depositInfo.getBalance().compareTo(BigDecimal.ZERO) < 0) {
                    sb.append("\n\nПохоже, у Вас нет активного счёта");
                    sb.append("\n\nДля увеличения счёта отправьте сумму: [ ≥5 TON ]").append(addedText).append("\n на адрес контракта:\n\n<code>").append(Main.ADDRESS_OF_INVEST_CONTRACT).append("</code>");
                } else {
                    DecimalFormat df = new DecimalFormat("#.###");
                    DateTime dateTime = new DateTime(depositInfo.getTime().longValue() * 1000L);
                    DateTimeFormatter dtfOut = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
                    sb.append("\n\nТекущий счёт:\n<b>").append(df.format(depositInfo.getBalance().stripTrailingZeros().doubleValue())).append(" TON</b>");
                    sb.append(" от ").append(dtfOut.print(dateTime));

                    BigInteger estimated = DepositChecker.getEstimatedProfit(user.getWallet());
                    sb.append("\n\nДоступная для вывода сумма: <b>").append(estimated.longValue() != 0L ? df.format(Utils.fromNano(estimated.longValue()).stripTrailingZeros().doubleValue()) : 0).append(" TON</b>");
                    if(!user.getReferralCode().isBlank())
                        sb.append("\nВаш партнёрский код: <code>").append(user.getReferralCode()).append("</code>");
                    sb.append("\n\nДля увеличения счёта отправьте сумму: [ ≥5 TON ] ").append(addedText).append("\n на адрес контракта:\n\n<code>").append(Main.ADDRESS_OF_INVEST_CONTRACT).append("</code>");
                }
                keyboard = new InlineKeyboardMarkup();
/*                keyboard.addRow(new InlineKeyboardButton("Пополнить").url("ton://transfer/EQC5O9RnkY80GohbmKgE2WKUc7e9T8GyMGSObZKGc_O8xkrS?amount=0&text=found"), new InlineKeyboardButton("Вывести").url("ton://transfer/EQC5O9RnkY80GohbmKgE2WKUc7e9T8GyMGSObZKGc_O8xkrS?amount=100000000&text=withdraw"));
                keyboard.addRow(new InlineKeyboardButton("Создать партнёрский код").callbackData("ref:create"));*/
                keyboard.addRow(new InlineKeyboardButton("Пополнить").callbackData("deposit"), new InlineKeyboardButton("Вывести").callbackData("withdraw"));
                if (user.getReferralCode().isBlank() && depositInfo.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                    keyboard.addRow(new InlineKeyboardButton("Создать партнёрский код").callbackData("ref:about"));
                } else {
                    keyboard.addRow(new InlineKeyboardButton("Партнёрская программа").callbackData("ref:about"));
                }
                keyboard.addRow(new InlineKeyboardButton("Изменить адрес").callbackData("wallet:change"));
                keyboard.addRow(new InlineKeyboardButton("Обновить").callbackData("menu:open"));
            }
            messageToSend = sb.toString();
        }


        if (messageToSend != null) {
            SendMessage sendMessage = new SendMessage(chatId, messageToSend);
            if (keyboard != null) sendMessage.replyMarkup(keyboard);
            sendMessage.disableWebPagePreview(true);
            sendMessage.parseMode(ParseMode.HTML);
            SendResponse sendResponse = Main.getBot().execute(sendMessage);
            System.out.println(sendResponse);
        }
    }
}
