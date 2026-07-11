package com.shippex.service.impl;

import com.shippex.dto.RegisterAppUserRequest;
import com.shippex.dto.RegisterAppUserResponse;
import com.shippex.dto.otp.GenerateOtpRequest;
import com.shippex.exception.OtpException;
import com.shippex.model.AppUser;
import com.shippex.repository.AppUserRepository;
import com.shippex.service.AppUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepository;
    private final OtpService otpService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AppUserServiceImpl(
            AppUserRepository appUserRepository,
            OtpService otpService,
            BCryptPasswordEncoder passwordEncoder) {

        this.appUserRepository = appUserRepository;
        this.otpService = otpService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public RegisterAppUserResponse createAppUserDetails(RegisterAppUserRequest request) {

        AppUser appUser = new AppUser();

        appUser.setName(request.getName());
        appUser.setUsername(request.getUsername());
        appUser.setPassword(passwordEncoder.encode(request.getPassword()));

        appUser.setEmail(request.getEmail());

        appUser.setCreatedAt(LocalDateTime.now());
        appUser.setLastUpdatedAt(LocalDateTime.now());

        appUser.setWhatsappContactNo(request.getWhatsappContactNo());
        appUser.setDesignation(request.getDesignation());
        appUser.setShipName(request.getShipName());
        appUser.setShipIMONumber(request.getShipIMONumber());

        log.info("Initiating OTP Check :: {}", appUser);
        if (!otpService.isOtpVerified(request.getWhatsappContactNo())) {
            log.info("OTP Check failed! Phone number is not verified  :: {}", appUser);
            throw new OtpException("Phone number is not verified.");
        }
        appUser.setVerified(Boolean.TRUE);
        log.info("OTP Check was success! :: {}", appUser);

        log.debug("Initiating new user creation :: {}", appUser);

        AppUser savedUser = new AppUser();
        try {
            savedUser = appUserRepository.save(appUser);
            log.info("User Created Successfully :: {}", savedUser);
        } catch (RuntimeException e) {
            log.error("Issue encountered while creating user!");
            throw e;
        }

        return RegisterAppUserResponse.builder()
                .username(savedUser.getUsername())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .whatsappContactNo(savedUser.getWhatsappContactNo())
                .designation(savedUser.getDesignation())
                .shipName(savedUser.getShipName())
                .shipIMONumber(savedUser.getShipIMONumber())
                .message("AppUser registered successfully.")
                .build();
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        Optional<AppUser> appUser = appUserRepository.findByUsername(username);
        return appUser.isEmpty();
    }

    @Override
    public AppUser getByUsername(String username) {
        log.info(
                "Fetching user details for username={}",
                username
        );

        return appUserRepository
                .findByUsername(username)
                .orElseThrow(() -> {

                    log.warn(
                            "User not found. username={}",
                            username
                    );

                    return new UsernameNotFoundException(
                            "User not found."
                    );
                });
    }
}
