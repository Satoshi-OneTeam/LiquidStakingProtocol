package com.vv.controllers;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import com.vv.Main;
import com.vv.MongoBase;
import com.vv.controllers.messages.CallbackMessage;
import com.vv.models.Status;
import com.vv.models.User;
import com.vv.ton.CodeGenerator;
import com.vv.ton.DepositChecker;
import com.vv.ton.DepositInfo;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.ton.java.utils.Utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.function.Function;

public class CallbackController {

    private static HashMap<String, Function<Update, CallbackMessage>> callbacks = new HashMap<>();

    static {
        callbacks.put("wallet", CallbackController::wallet);
        callbacks.put("menu", CallbackController::menu);
        callbacks.put("ref", CallbackController::ref);
        callbacks.put("deposit", CallbackController::deposit);
        callbacks.put("withdraw", CallbackController::withdraw);
    }

    public static void process(Update update) {
        Long chatId = update.callbackQuery().from().id();
        StatusController.cleanStatus(chatId);
        int messageId = update.callbackQuery().message().messageId();
        CallbackMessage callbackMessage = CallbackController.buildMessage(update);
        if (callbackMessage != null) {
            System.out.println("=====");
            System.out.println(callbackMessage.messageToSend);
            if (callbackMessage.keyboard instanceof ReplyKeyboardMarkup) {
                if (callbackMessage.messageToSend != null) {
                    System.out.println(callbackMessage.messageToSend);
                    try {
                        Main.getBot().execute(new DeleteMessage(chatId, messageId));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    SendMessage sendMessage = new SendMessage(chatId, callbackMessage.messageToSend);
                    if (!callbackMessage.isParseModeDisabled())
                        sendMessage.parseMode(ParseMode.HTML);
                    if (callbackMessage.keyboard != null)
                        sendMessage.replyMarkup(((ReplyKeyboardMarkup) callbackMessage.keyboard).resizeKeyboard(true));
                    SendResponse sendResponse = Main.getBot().execute(sendMessage);
                    System.out.println(sendResponse);
                }
            } else {
                if (callbackMessage.messageToSend != null) {
                    EditMessageText editMessageText = new EditMessageText(chatId, messageId,
                            callbackMessage.messageToSend);
                    if (callbackMessage.keyboard != null) {
                        if (callbackMessage.keyboard instanceof InlineKeyboardMarkup)
                            editMessageText.replyMarkup((InlineKeyboardMarkup) callbackMessage.keyboard);
                    }
                    if (!callbackMessage.isParseModeDisabled())
                        editMessageText.parseMode(ParseMode.HTML);
                    BaseResponse sendResponse = Main.getBot().execute(editMessageText);
                    System.out.println(sendResponse);
                }
            }
        }
    }

    private static CallbackMessage buildMessage(Update update) {
        String[] callbackData = update.callbackQuery().data().split(":");
        Function<Update, CallbackMessage> function = callbacks.get(callbackData[0]);
        CallbackMessage callbackMessage = null;
        if (function != null)
            callbackMessage = function.apply(update);
        return callbackMessage;
    }

    private static CallbackMessage deposit(Update update) {
        Long chatId = update.callbackQuery().from().id();
        String[] data = update.callbackQuery().data().split(":");
        User user = MongoBase.getInstance().getUser(chatId);
        String messageToSend = null;
        InlineKeyboardMarkup keyboard = null;
        String addedArgument = !user.getInvitedByCode().isBlank() ? "?text=" + user.getInvitedByCode() : "";
        String addedText = !user.getInvitedByCode().isBlank()
                ? " с комментарием <code>" + user.getInvitedByCode() + "</code>,"
                : "";
        keyboard = new InlineKeyboardMarkup();
        StringBuilder sb = new StringBuilder();
        DecimalFormat df = new DecimalFormat("#.###");
        DepositInfo depositInfo = DepositChecker.getDeposit(user.getWallet(), user);
        DateTime dateTime = new DateTime(depositInfo.getTime().longValue() * 1000L);
        DateTimeFormatter dtfOut = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        if (depositInfo.getBalance().compareTo(BigDecimal.ZERO) == 0
                || depositInfo.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            sb.append("Похоже, у Вас нет активного счёта!");
        } else {
            sb.append("Текущий счёт:\n<b>")
                    .append(df.format(depositInfo.getBalance().stripTrailingZeros().doubleValue())).append(" TON</b>");
            sb.append(" от ").append(dtfOut.print(dateTime));
        }
        sb.append(" \n\nДля увеличения личного счёта \nотправьте сумму: [ ≥5 TON ]\n\nна адрес контракта:\n\n").append(Main.ADDRESS_OF_INVEST_CONTRACT);
        messageToSend = sb.toString();
        keyboard.addRow(new InlineKeyboardButton("Пополнить")
                .url("ton://transfer/EQC5O9RnkY80GohbmKgE2WKUc7e9T8GyMGSObZKGc_O8xkrS?amount=0&text=found"));
        keyboard.addRow(new InlineKeyboardButton("Назад").callbackData("menu:open"));
        return new CallbackMessage(messageToSend, keyboard);
    }

    private static CallbackMessage withdraw(Update update) {
        Long chatId = update.callbackQuery().from().id();
        String[] data = update.callbackQuery().data().split(":");
        User user = MongoBase.getInstance().getUser(chatId);
        String messageToSend = null;
        InlineKeyboardMarkup keyboard = null;
        BigInteger estimated = DepositChecker.getEstimatedProfit(user.getWallet());

        StringBuilder sb = new StringBuilder();
        DecimalFormat df = new DecimalFormat("#.###");
        sb.append("Доступная для вывода сумма: <b>").append(estimated.longValue() > 0L ? df.format(Utils.fromNano(estimated.longValue()).stripTrailingZeros().doubleValue()) : 0).append(" TON</b>");
        sb.append("\n\nЧтобы вывести доступную сумму, отправьте: [0.1TON] \n\nс комментарием/MEMO: <code> [ withdraw ] </code>");
        sb.append("\n\nНа адрес контракта: \n<code>").append(Main.ADDRESS_OF_INVEST_CONTRACT).append("</code>");
        //sb.append("\n\nКомментарий/MEMO: <code> withdraw </code>");
        sb.append("\n\nКомментарий нужно отправить без скобок и пробелов");
        messageToSend = sb.toString();
        keyboard = new InlineKeyboardMarkup();
        keyboard.addRow(new InlineKeyboardButton("Вывести").url("ton://transfer/EQC5O9RnkY80GohbmKgE2WKUc7e9T8GyMGSObZKGc_O8xkrS?amount=100000000&text=withdraw"));
        keyboard.addRow(new InlineKeyboardButton("Назад").callbackData("menu:open"));
        return new CallbackMessage(messageToSend, keyboard);
    }

    private static CallbackMessage wallet(Update update) {
        Long chatId = update.callbackQuery().from().id();
        String[] data = update.callbackQuery().data().split(":");
        User user = MongoBase.getInstance().getUser(chatId);
        String messageToSend = null;
        InlineKeyboardMarkup keyboard = null;

        switch (data[1]) {
            case "change" -> {
                StatusController.statuses.put(chatId, Status.ADDRESS);
                StringBuilder sb = new StringBuilder();
                sb.append("Вы действительно хотите поменять кошелёк?");
                sb.append("\n\nВаш текущий кошелёк: ").append(user.getWallet());
                sb.append("\n\nВведите новый кошелёк, если хотите его изменить");
                messageToSend = sb.toString();
                keyboard = new InlineKeyboardMarkup();
                keyboard.addRow(new InlineKeyboardButton("Отменить").callbackData("menu:open"));
            }
            case "confirm" -> {
                String wallet = StatusController.text.get(chatId);
                MongoBase.getInstance().setWallet(chatId, wallet);
                StringBuilder sb = new StringBuilder();
                sb.append("Вы изменили кошелёк!");
                sb.append("\n\nВаш текущий кошелёк: ").append(wallet);
                messageToSend = sb.toString();
                keyboard = new InlineKeyboardMarkup();
                keyboard.addRow(new InlineKeyboardButton("Вернуться в меню").callbackData("menu:open"));
            }
        }
        return new CallbackMessage(messageToSend, keyboard);
    }

    private static CallbackMessage menu(Update update) {
        Long chatId = update.callbackQuery().from().id();
        String[] data = update.callbackQuery().data().split(":");
        User user = MongoBase.getInstance().getUser(chatId);
        String action = data[1];

        String messageToSend = null;
        InlineKeyboardMarkup keyboard = null;

        switch (action) {
            case "open" -> {
                StringBuilder sb = new StringBuilder();
                sb.append("<b>Добро пожаловать!</b>");
                if (user.getWallet().isBlank()) {
                    sb.append("\n\nДля продолжения работы с ботом введите свой TON кошелёк.");
                    StatusController.statuses.put(chatId, Status.ADDRESS);
                } else {
                    sb.append("\n\nЛичная информация пользователя в Farming Protocol TON");
                    sb.append("\n\n\uD83D\uDC8E ").append(user.getWallet());

                    String addedText = !user.getInvitedByCode().isBlank()
                            ? " с комментарием <code>" + user.getInvitedByCode() + "</code>,"
                            : "";

                    DepositInfo depositInfo = DepositChecker.getDeposit(user.getWallet(), user);
                    if (depositInfo.getBalance().compareTo(BigDecimal.ZERO) == 0
                            || depositInfo.getBalance().compareTo(BigDecimal.ZERO) < 0) {
                        sb.append("\n\nПохоже, у Вас нет активного счёта");
                        // sb.append("\n\nОтправьте любую сумму больше 1 TON на адрес контракта
                        // <code>").append(Main.ADDRESS_OF_INVEST_CONTRACT).append("</code>, чтобы
                        // получать вознаграждение.");
                        sb.append("\n\nДля увеличения счёта отправьте сумму: [≥ 5 TON]").append(addedText)
                                .append("\n на адрес контракта:\n\n<code>").append(Main.ADDRESS_OF_INVEST_CONTRACT)
                                .append("</code>");
                    } else {
                        DecimalFormat df = new DecimalFormat("#.###");
                        DateTime dateTime = new DateTime(depositInfo.getTime().longValue() * 1000L);
                        DateTimeFormatter dtfOut = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
                        sb.append("\n\nТекущий счёт:\n<b>")
                                .append(df.format(depositInfo.getBalance().stripTrailingZeros().doubleValue()))
                                .append(" TON</b>");
                        sb.append(" от ").append(dtfOut.print(dateTime));

                        BigInteger estimated = DepositChecker.getEstimatedProfit(user.getWallet());
                        sb.append("\n\nДоступная для вывода сумма: <b>")
                                .append(estimated.longValue() != 0L ? df.format(
                                        Utils.fromNano(estimated.longValue()).stripTrailingZeros().doubleValue()) : 0)
                                .append(" TON</b>");
                        if (!user.getReferralCode().isBlank())
                            sb.append("\n\nВаш партнёрский код: <code>").append(user.getReferralCode()).append("</code>");
                        sb.append(
                                "\n\nДецентрализованный фарминг протокол на блокчейне TON\n\n Поддержка: \n@farmingprotocolsupport");
                    }
                    keyboard = new InlineKeyboardMarkup();
                    // keyboard.addRow(new
                    // InlineKeyboardButton("Пополнить").url("https://app.tonkeeper.com/transfer/" +
                    // Main.ADDRESS_OF_INVEST_CONTRACT), new
                    // InlineKeyboardButton("Вывести").url("https://app.tonkeeper.com/transfer/" +
                    // Main.ADDRESS_OF_INVEST_CONTRACT));
                    keyboard.addRow(new InlineKeyboardButton("Пополнить").callbackData("deposit"),
                            new InlineKeyboardButton("Вывести").callbackData("withdraw"));
                    if (user.getReferralCode().isBlank()) {
                        if (depositInfo.getBalance().compareTo(BigDecimal.ZERO) > 0)
                            keyboard.addRow(
                                    new InlineKeyboardButton("Создать партнёрский код").callbackData("ref:about"));
                        else
                            keyboard.addRow(
                                    new InlineKeyboardButton("Партнёрская программа").callbackData("ref:about"));
                    }
                    keyboard.addRow(new InlineKeyboardButton("Изменить адрес").callbackData("wallet:change"));
                    keyboard.addRow(new InlineKeyboardButton("Обновить").callbackData("menu:open"));
                }
                messageToSend = sb.toString();
            }
        }
        return new CallbackMessage(messageToSend, keyboard);
    }

    private static CallbackMessage ref(Update update) {
        Long chatId = update.callbackQuery().from().id();
        String[] data = update.callbackQuery().data().split(":");
        User user = MongoBase.getInstance().getUser(chatId);
        String messageToSend = null;
        InlineKeyboardMarkup keyboard = null;

        String action = data[1];
        switch (action) {
            case "about" -> {
                keyboard = new InlineKeyboardMarkup();
                if (user.getReferralCode().isBlank()) {
                    DepositInfo depositInfo = DepositChecker.getDeposit(user.getWallet(), user);
                    if (depositInfo.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                        String refCodeString = CodeGenerator.generateCode();
                        MongoBase.getInstance().setReferralCode(user.getChatId(), refCodeString);
                        StringBuilder sb = new StringBuilder();
                        sb.append("<b>Ваш партнёрский код:</b> <code>").append(refCodeString).append("</code>");
                        sb.append("\n<b>Ссылка для приглашения:</b> <code>").append(Main.BOT_REF_URL)
                                .append(refCodeString).append("</code>");
                        sb.append(
                                "\n\nЗа каждого приглашенного пользователя, вы будете получать 5% бонуса на кошелёк, с которого вы принимаете участие в Farming Protocol TON!");
                        sb.append(
                                "\nДля того, чтобы получить бонус, приглашённый пользователь должен ввести ваш партнёрский код в виде комментария к платежу!");
                        messageToSend = sb.toString();
                        DepositChecker.sendMessage(refCodeString, user.getWallet());
                        // TODO: 7/10/2023 SENT TO CONTRACT
                    } else {
                        StringBuilder sb = new StringBuilder();
                        sb.append(
                                "Для того, чтобы воспользоваться партнёрской программой вам сперва необходимо пополнить счёт!");
                        sb.append(
                                "\n\nЗа каждого приглашённого пользователя, вы будете получать 5% бонуса на кошелёк, с которого вы принимаете участие в Farming Protocol TON!");
                        messageToSend = sb.toString();
                    }
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("<b>Ваш партнёрский код:</b> <code>").append(user.getReferralCode()).append("</code>");
                    sb.append("\n<b>Ссылка для приглашения:</b> <code>").append(Main.BOT_REF_URL)
                            .append(user.getReferralCode()).append("</code>");
                    sb.append(
                            "\n\nЗа каждого приглашённого пользователя, вы будете получать 5% бонуса на кошелёк, с которого вы принимаете участие в Farming Protocol TON!");
                    sb.append(
                            "\nДля того, чтобы получить бонус, приглашённый пользователь должен ввести ваш партнёрский код в виде комментария к платежу!");
                    messageToSend = sb.toString();
                }
                keyboard.addRow(new InlineKeyboardButton("Назад").callbackData("menu:open"));
            }
        }

        return new CallbackMessage(messageToSend, keyboard);
    }
}