package com.shippex.service;

import com.shippex.dto.RegisterAppUserRequest;
import com.shippex.dto.RegisterAppUserResponse;
import com.shippex.model.AppUser;
import com.shippex.dto.otp.VerifyOtpRequest;

public interface AppUserService {
    RegisterAppUserResponse createAppUserDetails(RegisterAppUserRequest appUser);
    boolean isUsernameAvailable(String username);
    AppUser getByUsername(String username);
    String getMaskedWhatsappContactNo(String username);
    void generatePasswordResetOtp(String username);
    void resetPassword(String username, String otp, String newPassword);
    AppUser getByWhatsappContactNo(String phoneNumber);
    void generatePasswordResetOtpForPhone(String phoneNumber);
    void resetPasswordByPhone(String phoneNumber, String otp, String newPassword);
    void updatePassword(String username, String currentPassword, String newPassword);
    void verifyAndUpdateWhatsappContactNo(String username, VerifyOtpRequest request);
    void updateName(String username, String name);
    void updateEmail(String username, String email);
    void updateDesignation(String username, com.shippex.constants.Designation designation);
    void updateShipName(String username, String shipName);
    void updateShipIMONumber(String username, String shipIMONumber);
}
