package com.shippex.service;

import com.shippex.dto.auth.LoginRequest;
import com.shippex.dto.auth.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

}