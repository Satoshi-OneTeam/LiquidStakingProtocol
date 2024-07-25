package com.vv.controllers;

import com.pengrad.telegrambot.model.ChatMember;
import com.pengrad.telegrambot.request.GetChatMember;
import com.pengrad.telegrambot.response.GetChatMemberResponse;
import com.vv.Main;

public class SubController {
    public static boolean isSubbed(Long chatId) {
        GetChatMemberResponse chatMember = Main.getBot().execute(new GetChatMember(Main.CHANNEL_ID, chatId));
        return chatMember.chatMember() != null && chatMember.chatMember().status() != ChatMember.Status.left;
    }
}
