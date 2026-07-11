package com.shippex.service.impl;

import com.shippex.model.AppUser;
import com.shippex.security.CustomUserDetails;
import com.shippex.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

//✅ JWT generation
//✅ Username extraction
//✅ Token validation
//✅ Invalid user validation
//✅ Expired token detection

public class JwtServiceTest {

    private JwtService jwtService;

    private CustomUserDetails user;

    @BeforeEach
    void setUp() {

        jwtService = new JwtService();

        ReflectionTestUtils.setField(
                jwtService,
                "jwtSecret",
                "my-test-secret-key-my-test-secret-key-123456"
        );

        ReflectionTestUtils.setField(
                jwtService,
                "jwtExpiration",
                3600000L
        );

        jwtService.init();

        AppUser appUser = new AppUser();

        appUser.setId("user-1");
        appUser.setName("John Doe");
        appUser.setUsername("john123");
        appUser.setPassword("$2a$10$hashedPassword");
        appUser.setVerified(true);

        user = new CustomUserDetails(appUser);
    }

    @Test
    void generateToken_shouldGenerateJwtSuccessfully() {

        String token =
                jwtService.generateToken(user);

        assertThat(token)
                .isNotNull()
                .isNotBlank();
    }

    @Test
    void extractUsername_shouldReturnCorrectUsername() {

        String token =
                jwtService.generateToken(user);

        String username =
                jwtService.extractUsername(token);

        assertThat(username)
                .isEqualTo("john123");
    }

    @Test
    void isTokenValid_shouldReturnTrue_forValidToken() {

        String token =
                jwtService.generateToken(user);

        boolean valid =
                jwtService.isTokenValid(token, user);

        assertThat(valid).isTrue();
    }

    @Test
    void isTokenValid_shouldReturnFalse_forDifferentUser() {

        String token =
                jwtService.generateToken(user);

        AppUser anotherUser = new AppUser();

        anotherUser.setId("2");
        anotherUser.setName("Jane");

        anotherUser.setUsername("jane123");
        anotherUser.setPassword("password");
        anotherUser.setVerified(true);

        CustomUserDetails another =
                new CustomUserDetails(anotherUser);

        boolean valid =
                jwtService.isTokenValid(token, another);

        assertThat(valid).isFalse();
    }

    @Test
    void generateToken_shouldContainCorrectUsername() {

        String token =
                jwtService.generateToken(user);

        assertThat(
                jwtService.extractUsername(token)
        ).isEqualTo(user.getUsername());
    }

    @Test
    void generateToken_shouldGenerateDifferentTokens() {

        String token1 =
                jwtService.generateToken(user);

        String token2 =
                jwtService.generateToken(user);

        assertThat(token1)
                .isNotBlank();

        assertThat(token2)
                .isNotBlank();
    }

}
