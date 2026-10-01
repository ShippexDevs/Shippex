package com.shippex.service;

import com.shippex.dto.auth.LoginRequest;
import com.shippex.dto.auth.LoginResponse;
import com.shippex.dto.otp.PhoneOtpRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request);
    LoginResponse loginWithWhatsappOtp(PhoneOtpRequest request);

}
