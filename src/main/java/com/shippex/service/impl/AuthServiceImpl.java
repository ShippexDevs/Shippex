package com.shippex.service.impl;

import com.shippex.dto.auth.LoginRequest;
import com.shippex.dto.auth.LoginResponse;
import com.shippex.security.CustomUserDetails;
import com.shippex.security.JwtService;
import com.shippex.service.AuthService;
import com.shippex.dto.otp.PhoneOtpRequest;
import com.shippex.dto.otp.VerifyOtpRequest;
import com.shippex.repository.AppUserRepository;
import com.shippex.model.AppUser;
import com.shippex.service.impl.OtpService;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;
    private final AppUserRepository appUserRepository;
    private final OtpService otpService;

    @Override
    public LoginResponse login(LoginRequest request) {
        log.debug("Authenticating user={}", request.getUsername());
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()
                        )
                );
        CustomUserDetails user =
                (CustomUserDetails) authentication.getPrincipal();
        log.info("Authentication successful for user={}", user.getUsername());
        String token = jwtService.generateToken(user);
        log.debug("JWT generated successfully for user={}", user.getUsername());
        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .username(user.getUsername())
                .name(user.getName())
                .build();
    }

    @Override
    public LoginResponse loginWithWhatsappOtp(PhoneOtpRequest request) {
        AppUser user = appUserRepository.findByWhatsappContactNo(request.getPhoneNumber())
                .orElseThrow(() -> new UsernameNotFoundException("No account found for this WhatsApp number."));
        CustomUserDetails userDetails = new CustomUserDetails(user);
        if (!userDetails.isEnabled()) {
            throw new DisabledException("Your account is disabled. Please contact support.");
        }
        VerifyOtpRequest otpRequest = new VerifyOtpRequest();
        otpRequest.setPhoneNumber(user.getWhatsappContactNo());
        otpRequest.setOtp(request.getOtp());
        otpService.verifyOtp(otpRequest);
        String token = jwtService.generateToken(userDetails);
        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .username(userDetails.getUsername())
                .name(userDetails.getName())
                .build();
    }
}
