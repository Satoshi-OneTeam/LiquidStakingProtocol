package com.vv.ton;

import com.vv.MongoBase;

import java.security.SecureRandom;
import java.util.Random;

public class CodeGenerator {
    public static String generateCode() {
        int leftLimit = 97;
        int rightLimit = 122;
        int targetStringLength = 8;
        SecureRandom random = new SecureRandom();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        while(!MongoBase.getInstance().checkIfUniqueCode(generatedString)) {
            generatedString = random.ints(leftLimit, rightLimit + 1)
                    .limit(targetStringLength)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
        }
        return generatedString;
    }
}
