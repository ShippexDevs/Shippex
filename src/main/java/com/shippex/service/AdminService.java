package com.shippex.service;

import com.shippex.dto.admin.*;
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
}
