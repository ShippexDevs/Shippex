package com.shippex.service;

public interface EmailService {
    void sendAdminCredentials(
            String recipientEmail,
            String adminName,
            String username,
            String temporaryPassword
    );
}
