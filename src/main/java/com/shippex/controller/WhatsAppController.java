package com.shippex.controller;

import com.shippex.dto.notification.NotificationRequest;
import com.shippex.dto.whatsapp.WhatsAppTextMessageRequest;
import com.shippex.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/whatsapp")
@RequiredArgsConstructor
public class WhatsAppController {

    private static final Logger logger =
            LoggerFactory.getLogger(WhatsAppController.class);

    private final NotificationService notificationService;

    @PostMapping("/send-test")
    public ResponseEntity<String> sendTestMessage(@RequestBody WhatsAppTextMessageRequest whatsAppTextMessageRequest) {

        logger.info("Received request to send WhatsApp message.");

        String s = notificationService.sendTextMessage(new NotificationRequest(whatsAppTextMessageRequest.getTo(),
                whatsAppTextMessageRequest.getText().getBody()));

        logger.info("Request processed successfully.");

        return ResponseEntity.ok(s);
    }

}