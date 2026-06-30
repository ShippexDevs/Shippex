package com.shippex.service;

import com.shippex.dto.notification.NotificationRequest;

public interface NotificationService {
    String sendTextMessage(NotificationRequest request);
}
