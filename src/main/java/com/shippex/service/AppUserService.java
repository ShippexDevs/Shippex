package com.shippex.service;

import com.shippex.dto.RegisterAppUserRequest;
import com.shippex.dto.RegisterAppUserResponse;
import com.shippex.model.AppUser;

public interface AppUserService {
    RegisterAppUserResponse createAppUserDetails(RegisterAppUserRequest appUser);
    boolean isUsernameAvailable(String username);
    AppUser getByUsername(String username);
}
