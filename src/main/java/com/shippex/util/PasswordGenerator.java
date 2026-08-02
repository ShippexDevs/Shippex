package com.shippex.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class PasswordGenerator {
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "@#$%&*!";

    private static final String ALL =
            UPPERCASE + LOWERCASE + DIGITS + SPECIAL;

    private static final int PASSWORD_LENGTH = 12;

    private final SecureRandom random = new SecureRandom();

    public String generateTemporaryPassword() {

        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);

        // Ensure at least one character from each category
        password.append(randomCharacter(UPPERCASE));
        password.append(randomCharacter(LOWERCASE));
        password.append(randomCharacter(DIGITS));
        password.append(randomCharacter(SPECIAL));

        // Fill remaining characters
        for (int i = 4; i < PASSWORD_LENGTH; i++) {
            password.append(randomCharacter(ALL));
        }

        // Shuffle characters
        return shuffle(password.toString());
    }

    private char randomCharacter(String source) {
        return source.charAt(random.nextInt(source.length()));
    }

    private String shuffle(String input) {

        char[] characters = input.toCharArray();

        for (int i = characters.length - 1; i > 0; i--) {

            int j = random.nextInt(i + 1);

            char temp = characters[i];
            characters[i] = characters[j];
            characters[j] = temp;
        }

        return new String(characters);
    }
}
