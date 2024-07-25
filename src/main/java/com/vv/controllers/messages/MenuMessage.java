package com.vv.controllers.messages;

import com.pengrad.telegrambot.model.request.Keyboard;
import com.vv.controllers.messages.AbstractMessage;

public class MenuMessage extends AbstractMessage {
    public MenuMessage() {
    }

    public MenuMessage(String messageToSend, Keyboard keyboard) {
        super(messageToSend, keyboard);
    }

    @Override
    public String toString() {
        return "MenuMessage{" +
                "messageToSend='" + messageToSend + '\'' +
                ", chatId=" + chatId +
                ", keyboard=" + keyboard +
                '}';
    }
}
