package com.vv.models;

import com.mongodb.BasicDBObject;
import org.bson.Document;

public class User {
    private Long chatId;
    private String wallet;
    private String referralCode;
    private String invitedByCode;

    public User(Long chatId, String invitedByCode) {
        this.chatId = chatId;
        this.wallet = "";
        this.invitedByCode = invitedByCode;
        this.referralCode = "";
    }

    public User(Document d) {
        this.chatId = d.getLong("chatId");
        this.wallet = d.getString("wallet");
        this.referralCode = d.getString("referralCode");
        this.invitedByCode = d.getString("invitedByCode");
        System.out.println(this.wallet);
    }

    public Document toDocument() {
        return new Document(new BasicDBObject("chatId", chatId)
                .append("wallet", wallet == null ? "" : wallet)
                .append("referralCode", referralCode)
                .append("invitedByCode", invitedByCode));
    }

    public Long getChatId() {
        return chatId;
    }

    public String getWallet() {
        return wallet;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public String getInvitedByCode() {
        return invitedByCode;
    }
}
