package com.shippex.service;

import com.shippex.dto.RegisterAppUserRequest;
import com.shippex.dto.RegisterAppUserResponse;

public interface AppUserService {
    RegisterAppUserResponse createAppUserDetails(RegisterAppUserRequest appUser);
    boolean isUsernameAvailable(String username);
}
