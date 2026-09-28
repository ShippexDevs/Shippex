package com.shippex.controller;

import com.shippex.dto.admin.AdminLoginRequest;
import com.shippex.dto.admin.AdminLoginResponse;
import com.shippex.dto.admin.AdminResponse;
import com.shippex.dto.admin.CreateAdminRequest;
import com.shippex.dto.admin.CreateAdminResponse;
import com.shippex.dto.admin.UpdateAdminRequest;
import com.shippex.service.AdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {
    @Mock AdminService adminService;
    @InjectMocks AdminController controller;

    @Test
    void createAdmin_returnsCreatedResponse() {
        CreateAdminRequest request = CreateAdminRequest.builder().username("new-admin").build();
        CreateAdminResponse result = CreateAdminResponse.builder().id("id-1").username("new-admin").build();
        when(adminService.createAdmin(request)).thenReturn(result);
        var response = controller.createAdmin(request);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(result, response.getBody().getData());
    }

    @Test
    void getAllAdmins_returnsEmptyResults() {
        when(adminService.getAllAdmins()).thenReturn(List.of());
        assertTrue(controller.getAllAdmins().getBody().getData().isEmpty());
    }

    @Test
    void getAdminAndUpdateAdmin_delegateIdsAndRequest() {
        AdminResponse result = new AdminResponse();
        UpdateAdminRequest request = new UpdateAdminRequest();
        when(adminService.getAdminById("id-1")).thenReturn(result);
        when(adminService.updateAdmin("id-1", request)).thenReturn(result);
        assertSame(result, controller.getAdminById("id-1").getBody().getData());
        assertSame(result, controller.updateAdmin("id-1", request).getBody().getData());
    }

    @Test
    void enableAndDisable_delegateToService() {
        assertEquals(HttpStatus.OK, controller.enableAdmin("id-1").getStatusCode());
        assertEquals(HttpStatus.OK, controller.disableAdmin("id-1").getStatusCode());
        verify(adminService).enableAdmin("id-1");
        verify(adminService).disableAdmin("id-1");
    }

    @Test
    void login_returnsServiceResponse() {
        AdminLoginRequest request = new AdminLoginRequest();
        AdminLoginResponse result = AdminLoginResponse.builder().accessToken("token").build();
        when(adminService.login(request)).thenReturn(result);
        assertSame(result, controller.login(request).getBody());
    }
}
