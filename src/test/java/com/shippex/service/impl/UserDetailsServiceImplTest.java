package com.shippex.service.impl;

import com.shippex.constants.AccountStatus;
import com.shippex.constants.Role;
import com.shippex.model.AdminUser;
import com.shippex.model.AppUser;
import com.shippex.repository.AdminUserRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private AdminUserRepository adminUserRepository;

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
        appUser.setRole(Role.USER);
        appUser.setAccountStatus(AccountStatus.ACTIVE);
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

        assertThat(user.getRole())
                .isEqualTo(Role.USER);

        assertThat(user.isEnabled())
                .isTrue();

        verify(appUserRepository)
                .findByUsername("john123");

        verifyNoInteractions(adminUserRepository);
    }

    @Test
    void loadUserByUsername_shouldReturnAdminUserDetails() {

        AdminUser adminUser = new AdminUser();

        adminUser.setId("admin-1");
        adminUser.setName("Admin User");
        adminUser.setUsername("admin123");
        adminUser.setPassword("$2a$10$hashedPassword");
        adminUser.setRole(Role.ADMIN);
        adminUser.setAccountStatus(AccountStatus.ACTIVE);

        when(appUserRepository.findByUsername("admin123"))
                .thenReturn(Optional.empty());

        when(adminUserRepository.findByUsername("admin123"))
                .thenReturn(Optional.of(adminUser));

        UserDetails result =
                userDetailsService.loadUserByUsername("admin123");

        assertThat(result)
                .isInstanceOf(CustomUserDetails.class);

        CustomUserDetails user =
                (CustomUserDetails) result;

        assertThat(user.getId())
                .isEqualTo("admin-1");

        assertThat(user.getName())
                .isEqualTo("Admin User");

        assertThat(user.getUsername())
                .isEqualTo("admin123");

        assertThat(user.getRole())
                .isEqualTo(Role.ADMIN);

        assertThat(user.isEnabled())
                .isTrue();

        verify(appUserRepository)
                .findByUsername("admin123");

        verify(adminUserRepository)
                .findByUsername("admin123");
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenUserDoesNotExist() {

        when(appUserRepository.findByUsername("john123"))
                .thenReturn(Optional.empty());

        when(adminUserRepository.findByUsername("john123"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userDetailsService.loadUserByUsername("john123"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found: john123");

        verify(appUserRepository)
                .findByUsername("john123");

        verify(adminUserRepository)
                .findByUsername("john123");
    }

    @Test
    void loadUserByUsername_shouldReturnDisabledUser_whenAccountIsDisabled() {

        appUser.setAccountStatus(AccountStatus.DISABLED);

        when(appUserRepository.findByUsername("john123"))
                .thenReturn(Optional.of(appUser));

        CustomUserDetails user =
                (CustomUserDetails)
                        userDetailsService.loadUserByUsername("john123");

        assertThat(user.isEnabled())
                .isFalse();

        verify(appUserRepository)
                .findByUsername("john123");

        verifyNoInteractions(adminUserRepository);
    }

    @Test
    void loadUserByUsername_shouldReturnCorrectRoleForAppUser() {

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

        verify(appUserRepository)
                .findByUsername("john123");

        verifyNoInteractions(adminUserRepository);
    }
}