package com.shippex.service.impl;

import com.shippex.model.AppUser;
import com.shippex.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class CustomUserDetailsTest {

    private AppUser appUser;

    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {

        appUser = new AppUser();

        appUser.setId("user-1");
        appUser.setName("John Doe");
        appUser.setUsername("john123");
        appUser.setPassword("$2a$10$hashedPassword");
        appUser.setVerified(true);

        userDetails = new CustomUserDetails(appUser);
    }

    @Test
    void getUsername_shouldReturnUsername() {

        assertThat(userDetails.getUsername())
                .isEqualTo("john123");
    }

    @Test
    void getPassword_shouldReturnPassword() {

        assertThat(userDetails.getPassword())
                .isEqualTo("$2a$10$hashedPassword");
    }

    @Test
    void getId_shouldReturnUserId() {

        assertThat(userDetails.getId())
                .isEqualTo("user-1");
    }

    @Test
    void getName_shouldReturnName() {

        assertThat(userDetails.getName())
                .isEqualTo("John Doe");
    }

    @Test
    void getAuthorities_shouldReturnRoleUser() {

        Collection<? extends SimpleGrantedAuthority> authorities =
                (Collection<? extends SimpleGrantedAuthority>)
                        userDetails.getAuthorities();

        assertThat(authorities)
                .hasSize(1);

        assertThat(authorities)
                .extracting(SimpleGrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    void isEnabled_shouldReturnTrue_whenUserIsVerified() {

        assertThat(userDetails.isEnabled())
                .isTrue();
    }

    @Test
    void isEnabled_shouldReturnFalse_whenUserIsNotVerified() {

        appUser.setVerified(false);

        userDetails = new CustomUserDetails(appUser);

        assertThat(userDetails.isEnabled())
                .isFalse();
    }

    @Test
    void isEnabled_shouldReturnFalse_whenVerifiedIsNull() {

        appUser.setVerified(null);

        userDetails = new CustomUserDetails(appUser);

        assertThat(userDetails.isEnabled())
                .isFalse();
    }

    @Test
    void isAccountNonExpired_shouldAlwaysReturnTrue() {

        assertThat(userDetails.isAccountNonExpired())
                .isTrue();
    }

    @Test
    void isAccountNonLocked_shouldAlwaysReturnTrue() {

        assertThat(userDetails.isAccountNonLocked())
                .isTrue();
    }

    @Test
    void isCredentialsNonExpired_shouldAlwaysReturnTrue() {

        assertThat(userDetails.isCredentialsNonExpired())
                .isTrue();
    }

    @Test
    void getAppUser_shouldReturnUnderlyingUser() {

        assertThat(userDetails.getAppUser())
                .isSameAs(appUser);
    }
}