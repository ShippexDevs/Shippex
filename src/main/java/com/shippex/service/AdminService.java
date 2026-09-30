package com.shippex.service;

import com.shippex.dto.admin.*;
import com.shippex.dto.auth.CurrentUserResponse;
import org.springframework.stereotype.Service;

import java.util.List;


public interface AdminService {
    CreateAdminResponse createAdmin(CreateAdminRequest request);
    List<AdminResponse> getAllAdmins();
    AdminResponse getAdminById(String id);
    AdminResponse updateAdmin(String id , UpdateAdminRequest request);
    void enableAdmin(String id);
    void disableAdmin(String id);

    AdminLoginResponse login(AdminLoginRequest request);

    List<CurrentUserResponse> getAllAppUsers();
    CurrentUserResponse updateAppUserStatus(
            String userId,
            boolean enabled
    );

}
