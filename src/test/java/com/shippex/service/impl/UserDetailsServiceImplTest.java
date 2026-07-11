package com.shippex.service.impl;

import com.shippex.model.AppUser;
import com.shippex.repository.AppUserRepository;
import com.shippex.security.CustomUserDetails;
import com.shippex.service.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

//✅ Existing user returns CustomUserDetails
//✅ User not found throws UsernameNotFoundException
//✅ Unverified users are disabled (isEnabled() == false)
//✅ ROLE_USER is assigned correctly
//✅ Repository interaction occurs exactly as expected

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private AppUser appUser;

    @BeforeEach
    void setUp() {

        appUser = new AppUser();

        appUser.setId("user-1");
        appUser.setName("John Doe");
        appUser.setUsername("john123");
        appUser.setPassword("$2a$10$hashedPassword");
        appUser.setVerified(true);
    }

    @Test
    void loadUserByUsername_shouldReturnCustomUserDetails() {

        when(appUserRepository.findByUsername("john123"))
                .thenReturn(Optional.of(appUser));

        UserDetails result =
                userDetailsService.loadUserByUsername("john123");

        assertThat(result)
                .isInstanceOf(CustomUserDetails.class);

        CustomUserDetails user =
                (CustomUserDetails) result;

        assertThat(user.getId())
                .isEqualTo("user-1");

        assertThat(user.getName())
                .isEqualTo("John Doe");

        assertThat(user.getUsername())
                .isEqualTo("john123");

        assertThat(user.getPassword())
                .isEqualTo("$2a$10$hashedPassword");

        assertThat(user.isEnabled())
                .isTrue();

        verify(appUserRepository)
                .findByUsername("john123");
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenUserDoesNotExist() {

        when(appUserRepository.findByUsername("john123"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userDetailsService.loadUserByUsername("john123"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found.");

        verify(appUserRepository)
                .findByUsername("john123");
    }

    @Test
    void loadUserByUsername_shouldReturnDisabledUser_whenUserIsNotVerified() {

        appUser.setVerified(false);

        when(appUserRepository.findByUsername("john123"))
                .thenReturn(Optional.of(appUser));

        CustomUserDetails user =
                (CustomUserDetails)
                        userDetailsService.loadUserByUsername("john123");

        assertThat(user.isEnabled())
                .isFalse();
    }

    @Test
    void loadUserByUsername_shouldAlwaysAssignRoleUser() {

        when(appUserRepository.findByUsername("john123"))
                .thenReturn(Optional.of(appUser));

        CustomUserDetails user =
                (CustomUserDetails)
                        userDetailsService.loadUserByUsername("john123");

        assertThat(user.getAuthorities())
                .hasSize(1);

        assertThat(user.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }
}