package com.shippex.controller;

import com.shippex.dto.whatsapp.WhatsAppText;
import com.shippex.dto.whatsapp.WhatsAppTextMessageRequest;
import com.shippex.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WhatsAppControllerTest {
    @Mock NotificationService notificationService;
    @InjectMocks WhatsAppController controller;

    @Test
    void sendTestMessageSendsRecipientAndBody() {
        WhatsAppTextMessageRequest request = new WhatsAppTextMessageRequest();
        request.setTo("+15550000000");
        request.setText(new WhatsAppText("Test message"));
        when(notificationService.sendTextMessage(any())).thenReturn("wamid-1");

        var response = controller.sendTestMessage(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("wamid-1", response.getBody());
        verify(notificationService).sendTextMessage(argThat(message ->
                message.getRecipient().equals("+15550000000") && message.getMessage().equals("Test message")));
    }

    @Test
    void sendTestMessagePropagatesProviderFailure() {
        WhatsAppTextMessageRequest request = new WhatsAppTextMessageRequest();
        request.setTo("+15550000000");
        request.setText(new WhatsAppText("Test message"));
        when(notificationService.sendTextMessage(any())).thenThrow(new IllegalStateException("provider failed"));
        assertThrows(IllegalStateException.class, () -> controller.sendTestMessage(request));
    }
}
