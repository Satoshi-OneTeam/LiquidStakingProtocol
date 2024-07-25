package com.vv;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import com.vv.models.User;
import org.bson.Document;

public class MongoBase {
    private static MongoBase instance;
    private MongoClient mongoClient;
    private MongoDatabase db;


    public static MongoBase getInstance() {
        if (instance == null) instance = new MongoBase();
        return instance;
    }

    private MongoBase() {
        String user = "test";
        String database = "admin";
        char[] password = "test".toCharArray();
        MongoCredential credential = MongoCredential.createCredential(user, database, password);
        MongoClientOptions options = MongoClientOptions.builder().build();
        mongoClient =
                Main.IS_DEBUG ?
                        new MongoClient(new ServerAddress("127.0.0.1", 27017), credential, options) :
                        new MongoClient(new ServerAddress("127.0.0.1", 27017));
        db = mongoClient.getDatabase("invest");
    }

    public void addUser(User user) {
        db.getCollection("users").insertOne(user.toDocument());
    }

    public User getUser(Long chatId) {
        Document d = db.getCollection("users").find(new BasicDBObject("chatId", chatId)).first();
        User user = null;
        if(d != null) user = new User(d);
        else {
            user = new User(chatId, "");
            this.addUser(user);
        }
        return user;
    }

    public boolean checkIfUniqueCode(String code) {
        Document d = db.getCollection("users").find(new BasicDBObject("referralCode", code)).first();
        return d == null;
    }

    public User getUser(Long chatId, String invitedBy) {
        Document d = db.getCollection("users").find(new BasicDBObject("chatId", chatId)).first();
        User user = null;
        if(d != null) user = new User(d);
        else {
            user = new User(chatId, invitedBy);
            this.addUser(user);
        }

        return user;
    }

    public void setWallet(Long chatId, String wallet) {
        db.getCollection("users").updateOne(new BasicDBObject("chatId", chatId), new BasicDBObject("$set", new BasicDBObject("wallet", wallet)));
    }

    public void getReferralCodeByUserWallet(String wallet) {

    }

    public void setReferralCode(Long chatId, String refCode) {
        db.getCollection("users").updateOne(new BasicDBObject("chatId", chatId), new BasicDBObject("$set", new BasicDBObject("referralCode", refCode)));
    }
}
