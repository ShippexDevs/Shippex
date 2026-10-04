package com.shippex.service.impl;

import com.shippex.constants.AccountStatus;
import com.shippex.constants.Role;
import com.shippex.dto.RegisterAppUserRequest;
import com.shippex.dto.RegisterAppUserResponse;
import com.shippex.dto.otp.GenerateOtpRequest;
import com.shippex.exception.DuplicateUsernameException;
import com.shippex.exception.OtpException;
import com.shippex.model.AppUser;
import com.shippex.repository.AdminUserRepository;
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
import com.shippex.constants.Designation;
import com.shippex.dto.otp.VerifyOtpRequest;
import com.shippex.dto.otp.GenerateOtpRequest;

@Slf4j
@Service
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepository;
    private final AdminUserRepository adminUserRepository;
    private final OtpService otpService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AppUserServiceImpl(
            AppUserRepository appUserRepository,
            OtpService otpService,
            BCryptPasswordEncoder passwordEncoder,
            AdminUserRepository adminUserRepository) {

        this.appUserRepository = appUserRepository;
        this.adminUserRepository = adminUserRepository;
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
        appUser.setRole(Role.USER);
        appUser.setAccountStatus(AccountStatus.ACTIVE);

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

        boolean appUserExists =
                appUserRepository.existsByUsername(username);

        boolean adminUserExists =
                adminUserRepository.existsByUsername(username);

        return !appUserExists && !adminUserExists;
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

    @Override
    public String getMaskedWhatsappContactNo(String username) {
        String phoneNumber = getByUsername(username).getWhatsappContactNo();
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalStateException("No WhatsApp number is associated with this account.");
        }
        String digits = phoneNumber.replaceAll("\\D", "");
        if (digits.length() <= 4) {
            return "*".repeat(digits.length());
        }
        return digits.substring(0, 2)
                + "*".repeat(digits.length() - 4)
                + digits.substring(digits.length() - 2);
    }

    @Override
    public void generatePasswordResetOtp(String username) {
        String phoneNumber = getByUsername(username).getWhatsappContactNo();
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalStateException("No WhatsApp number is associated with this account.");
        }
        GenerateOtpRequest request = new GenerateOtpRequest();
        request.setPhoneNumber(phoneNumber);
        otpService.generateOtp(request);
    }

    @Override
    public void resetPassword(String username, String otp, String newPassword) {
        AppUser user = getByUsername(username);
        if (user.getWhatsappContactNo() == null || user.getWhatsappContactNo().isBlank()) {
            throw new IllegalStateException("No WhatsApp number is associated with this account.");
        }
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setPhoneNumber(user.getWhatsappContactNo());
        request.setOtp(otp);
        otpService.verifyOtp(request);
        updateUser(user, updated -> updated.setPassword(passwordEncoder.encode(newPassword)));
    }

    @Override
    public AppUser getByWhatsappContactNo(String phoneNumber) {
        return appUserRepository.findByWhatsappContactNo(phoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException("No account found for this WhatsApp number."));
    }

    @Override
    public void generatePasswordResetOtpForPhone(String phoneNumber) {
        AppUser user = getByWhatsappContactNo(phoneNumber);
        GenerateOtpRequest request = new GenerateOtpRequest();
        request.setPhoneNumber(user.getWhatsappContactNo());
        otpService.generateOtp(request);
    }

    @Override
    public void resetPasswordByPhone(String phoneNumber, String otp, String newPassword) {
        AppUser user = getByWhatsappContactNo(phoneNumber);
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setPhoneNumber(user.getWhatsappContactNo());
        request.setOtp(otp);
        otpService.verifyOtp(request);
        updateUser(user, updated -> updated.setPassword(passwordEncoder.encode(newPassword)));
    }

    @Override
    public void updatePassword(String username, String currentPassword, String newPassword) {
        AppUser user = getByUsername(username);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        updateUser(user, updated -> updated.setPassword(passwordEncoder.encode(newPassword)));
    }

    @Override
    public void verifyAndUpdateWhatsappContactNo(String username, VerifyOtpRequest request) {
        AppUser user = getByUsername(username);
        otpService.verifyOtp(request);
        updateUser(user, updated -> updated.setWhatsappContactNo(request.getPhoneNumber()));
    }

    @Override
    public void updateName(String username, String name) {
        updateUser(username, user -> user.setName(name));
    }

    @Override
    public void updateEmail(String username, String email) {
        updateUser(username, user -> user.setEmail(email));
    }

    @Override
    public void updateDesignation(String username, Designation designation) {
        updateUser(username, user -> user.setDesignation(designation));
    }

    @Override
    public void updateShipName(String username, String shipName) {
        updateUser(username, user -> user.setShipName(shipName));
    }

    @Override
    public void updateShipIMONumber(String username, String shipIMONumber) {
        updateUser(username, user -> user.setShipIMONumber(shipIMONumber));
    }

    private void updateUser(String username, java.util.function.Consumer<AppUser> update) {
        updateUser(getByUsername(username), update);
    }

    private void updateUser(AppUser user, java.util.function.Consumer<AppUser> update) {
        update.accept(user);
        user.setLastUpdatedAt(LocalDateTime.now());
        appUserRepository.save(user);
    }
}
