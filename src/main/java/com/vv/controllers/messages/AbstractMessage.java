package com.vv.controllers.messages;

import com.pengrad.telegrambot.model.request.Keyboard;

public abstract class AbstractMessage {
    public String messageToSend;
    public Long chatId;
    public Keyboard keyboard;

    public String getMessageToSend() {
        return messageToSend;
    }

    public void setMessageToSend(String messageToSend) {
        this.messageToSend = messageToSend;
    }

    public Long getChatId() {
        return chatId;
    }

    public AbstractMessage() {}

    public AbstractMessage(String messageToSend, Keyboard keyboard) {
        this.messageToSend = messageToSend;
        this.keyboard = keyboard;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Keyboard getKeyboard() {
        return keyboard;
    }

    public AbstractMessage setKeyboard(Keyboard keyboard) {
        this.keyboard = keyboard;
        return this;
    }
}
