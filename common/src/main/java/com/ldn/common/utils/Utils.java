package com.ldn.common.utils;

import java.security.SecureRandom;

public class Utils {
    private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    public static String generateBase32Secret() {
        SecureRandom random = new SecureRandom();
        StringBuilder secret = new StringBuilder(32);

        for (int i = 0; i < 32; i++) {
            int randomIndex = random.nextInt(BASE32_CHARS.length());
            secret.append(BASE32_CHARS.charAt(randomIndex));
        }

        return secret.toString();
        // Example "JBSWY3DPEHPK3PXP234567ABCDEFGHIJKLMNOP"
    }
}
