package com.shippex.service;

import com.shippex.dto.admin.ChangePasswordRequest;
import com.shippex.dto.admin.ChangePasswordResponse;
import com.shippex.security.CustomUserDetails;

public interface AdminAuthService {
    ChangePasswordResponse changePassword(
            ChangePasswordRequest request,
            CustomUserDetails currentAdmin
    );
}
