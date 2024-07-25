package com.vv.controllers;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.vv.Main;

public class UpdateController {
    public static void process(Update update) {

        Long chatId = null;
        if (update.message() != null) {
            chatId = update.message().from().id();
        } else if (update.callbackQuery() != null) {
            chatId = update.callbackQuery().from().id();
        }

        if (chatId != null) {
            boolean isSubbed = SubController.isSubbed(chatId);
            if (isSubbed) {
                if (update.message() != null) {
                    if(StatusController.isStatus(chatId)) {
                        StatusController.process(update);
                    } else if(MenuController.isMenuCommand(update.message().text())) {
                        MenuController.process(update);
                    } else MessageController.process(update);
                } else if (update.callbackQuery() != null) {
                    CallbackController.process(update);
                }
            } else {
                SendMessage sendMessage = new SendMessage(chatId, "Подпишитесь на канал!");
                InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
                keyboard.addRow(new InlineKeyboardButton("Канал").url(Main.CHANNEL_URL));
                keyboard.addRow(new InlineKeyboardButton("Подписался!").callbackData("menu:open"));
                sendMessage.replyMarkup(keyboard);
                Main.getBot().execute(sendMessage);
            }
        }
    }
}
