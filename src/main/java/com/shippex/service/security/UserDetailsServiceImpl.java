package com.shippex.service.security;

import com.shippex.model.AdminUser;
import com.shippex.model.AppUser;
import com.shippex.repository.AdminUserRepository;
import com.shippex.repository.AppUserRepository;
import com.shippex.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {
    private final AppUserRepository appUserRepository;
    private final AdminUserRepository adminUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        AppUser appUser = appUserRepository
                .findByUsername(username)
                .orElse(null);

        if (appUser != null) {
            log.debug("Authenticated AppUser: {}", username);
            return new CustomUserDetails(appUser);
        }

        AdminUser adminUser = adminUserRepository
                .findByUsername(username)
                .orElse(null);

        if (adminUser != null) {
            log.debug("Authenticated AdminUser: {}", username);
            return new CustomUserDetails(adminUser);
        }

        throw new UsernameNotFoundException(
                "User not found: " + username
        );
    }
}
