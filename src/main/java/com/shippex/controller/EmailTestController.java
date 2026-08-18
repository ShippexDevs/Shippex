package com.shippex.controller;

import com.shippex.service.AdminService;
import com.shippex.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class EmailTestController {
    private final AdminService adminService;
    private final EmailService emailService;

    @PostMapping("/email")
    public ResponseEntity<String> sendEmail(){
        emailService.sendAdminCredentials(
                "santojeetchakraborty9432862795@gmail.com",
                "Santojeet Chakraborty",
                "santojeet.admin",
                "Temp@1234"
        );
        return ResponseEntity.ok("Email Sent successfully.");
    }
}
