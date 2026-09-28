package com.shippex.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordGeneratorTest {
    private final PasswordGenerator generator = new PasswordGenerator();

    @Test
    void generatedPasswordHasExpectedLengthAndEveryRequiredCharacterClass() {
        String password = generator.generateTemporaryPassword();
        assertEquals(12, password.length());
        assertTrue(password.chars().anyMatch(Character::isUpperCase));
        assertTrue(password.chars().anyMatch(Character::isLowerCase));
        assertTrue(password.chars().anyMatch(Character::isDigit));
        assertTrue(password.chars().anyMatch(c -> "@#$%&*!".indexOf(c) >= 0));
    }

    @Test
    void generatedPasswordsAreNotConstantAcrossInvocations() {
        assertNotEquals(generator.generateTemporaryPassword(), generator.generateTemporaryPassword());
    }
}
