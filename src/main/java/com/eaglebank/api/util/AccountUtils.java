package com.eaglebank.api.util;

import java.util.Random;

public class AccountUtils {
    private static final Random RANDOM = new Random();


    public static String generateUniqueAccountNumber() {
        StringBuilder sb = new StringBuilder("01");
        for (int i = 0; i < 6; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    public static String generateSortCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            if (i > 0) sb.append("-");
            int part = RANDOM.nextInt(90) + 10; // ensures two digits, not starting with 0
            sb.append(part);
        }
        return sb.toString();
    }
}
