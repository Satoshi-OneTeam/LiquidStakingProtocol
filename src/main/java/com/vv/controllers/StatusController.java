package com.vv.controllers;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.vv.Main;
import com.vv.models.Status;
import org.ton.java.address.Address;

import java.util.HashMap;

public class StatusController {
    public static HashMap<Long, Status> statuses = new HashMap<>();
    public static HashMap<Long, String> text = new HashMap<>();

    public static void process(Update update) {
        Long chatId = update.message().from().id();
        String message = update.message().text();
        Status status = statuses.get(chatId);

        String messageToSend = null;
        InlineKeyboardMarkup keyboard = null;

        switch (status) {
            case ADDRESS -> {
                keyboard = new InlineKeyboardMarkup();
                StringBuilder sb = new StringBuilder();
                sb.append("Для продолжения работы с ботом необходимо ввести свой кошелёк");
                try {
                    Address address = Address.of(message);
                    text.put(chatId, message);
                    sb.append("\n\nВы ввели: ").append(address.toString(true, true, true));
                    keyboard.addRow(new InlineKeyboardButton("Подтвердить").callbackData("wallet:confirm"));
                    keyboard.addRow(new InlineKeyboardButton("Отменить").callbackData("menu:cancel"));
                } catch (Exception | Error e) {
                    e.printStackTrace();
                    sb.append("\n\nВы ввели: ").append(message);
                    sb.append("\nТакого адреса не существует! Введите ещё раз!");
                    keyboard.addRow(new InlineKeyboardButton("Отменить").callbackData("wallet:cancel"));
                }
                messageToSend = sb.toString();
            }
            case WITHDRAW -> {

            }
            case DEPOSIT -> {

            }
        }

        if (messageToSend != null) {
            SendMessage sendMessage = new SendMessage(chatId, messageToSend);
            if (keyboard != null) sendMessage.replyMarkup(keyboard);
            sendMessage.disableWebPagePreview(true);
            sendMessage.parseMode(ParseMode.HTML);
            Main.getBot().execute(sendMessage);
        }
    }

    public static boolean isStatus(Long chatId) {
        Status status = statuses.get(chatId);
        return status != null && status != Status.NONE;
    }

    public static void cleanStatus(Long chatId) {
        statuses.put(chatId, Status.NONE);
    }
}
