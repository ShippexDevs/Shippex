package com.shippex.service.impl;

import jakarta.mail.Session;
import jakarta.mail.Multipart;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {
    @Mock JavaMailSender mailSender;
    @InjectMocks EmailServiceImpl service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "senderEmail", "noreply@shippex.test");
    }

    @Test
    void sendAdminCredentials_sendsHtmlWelcomeEmail() throws Exception {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        service.sendAdminCredentials("ada@example.com", "Ada", "ada", "Temp123!");

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());
        MimeMessage sent = captor.getValue();
        assertEquals("ada@example.com", sent.getAllRecipients()[0].toString());
        assertEquals("noreply@shippex.test", sent.getFrom()[0].toString());
        assertEquals("Welcome to Shippex - Admin Account Created", sent.getSubject());
        String body = contentText(sent.getContent());
        assertTrue(body.contains("Ada"));
        assertTrue(body.contains("ada"));
        assertTrue(body.contains("Temp123!"));
        assertTrue(body.contains("Please login and change your password immediately."));
    }

    private String contentText(Object content) throws Exception {
        if (!(content instanceof Multipart multipart)) {
            return content.toString();
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < multipart.getCount(); i++) {
            result.append(contentText(multipart.getBodyPart(i).getContent()));
        }
        return result.toString();
    }

    @Test
    void sendAdminCredentials_wrapsMailTransportFailure() {
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(new Properties())));
        doThrow(new MailSendException("SMTP unavailable")).when(mailSender).send(any(MimeMessage.class));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> service.sendAdminCredentials("ada@example.com", "Ada", "ada", "Temp123!"));

        assertEquals("Unable to send email.", exception.getMessage());
    }

}
