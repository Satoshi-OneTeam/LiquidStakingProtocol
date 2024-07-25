package com.vv.controllers.messages;

import com.pengrad.telegrambot.model.request.Keyboard;

public class CallbackMessage extends AbstractMessage {
    private String callbackData;
    private boolean parseModeDisabled = false;
    private boolean disableWebPreview = true;

    public CallbackMessage(String messageToSend, Keyboard keyboard) {
        super(messageToSend, keyboard);
    }

    public CallbackMessage(String messageToSend, Keyboard keyboard, boolean parseModeDisabled) {
        super(messageToSend, keyboard);
        this.parseModeDisabled = parseModeDisabled;
    }

    public CallbackMessage(String messageToSend, Keyboard keyboard, boolean parseModeDisabled, boolean disableWebPreview) {
        super(messageToSend, keyboard);
        this.parseModeDisabled = parseModeDisabled;
        this.disableWebPreview = disableWebPreview;
    }

    public CallbackMessage(String messageToSend) {
        this.messageToSend = messageToSend;
    }

    public CallbackMessage(String callbackData, String messageToSend) {
        this.callbackData = callbackData;
        this.messageToSend = messageToSend;
    }

    public boolean isParseModeDisabled() {
        return parseModeDisabled;
    }

    public CallbackMessage() {
    }

    @Override
    public String getMessageToSend() {
        return messageToSend;
    }

    public String getCallbackData() {
        return callbackData;
    }

    @Override
    public String toString() {
        return "CallbackMessage{" +
                "callbackData='" + callbackData + '\'' +
                ", messageToSend='" + messageToSend + '\'' +
                ", chatId=" + chatId +
                ", keyboard=" + keyboard +
                '}';
    }
}
