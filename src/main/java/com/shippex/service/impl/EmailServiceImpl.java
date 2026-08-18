package com.shippex.service.impl;

import com.shippex.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Override
    public void sendAdminCredentials(
            String recipientEmail,
            String adminName,
            String username,
            String temporaryPassword
    ) {

        log.info("Sending admin credentials to {}", recipientEmail);

        try {

            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    true,
                    "UTF-8"
            );

            helper.setFrom(senderEmail);
            helper.setTo(recipientEmail);
            helper.setSubject("Welcome to Shippex - Admin Account Created");

            helper.setText(
                    buildAdminWelcomeEmail(
                            adminName,
                            username,
                            temporaryPassword
                    ),
                    true
            );

            mailSender.send(mimeMessage);

            log.info("Admin credential email sent successfully to {}", recipientEmail);

        } catch (MessagingException | MailException exception) {

            log.error(
                    "Failed to send admin credential email to {}",
                    recipientEmail,
                    exception
            );

            throw new RuntimeException("Unable to send email.");
        }
    }

    private String buildAdminWelcomeEmail(
            String adminName,
            String username,
            String temporaryPassword
    ) {

        return """
            <!DOCTYPE html>
            <html>
            <body>

            <h2>Welcome to Shippex</h2>

            <p>Hello <strong>%s</strong>,</p>

            <p>Your administrator account has been created successfully.</p>

            <p><strong>Username:</strong> %s</p>

            <p><strong>Temporary Password:</strong> %s</p>

            <p>Please login and change your password immediately.</p>

            <p>Regards,<br>Shippex Team</p>

            </body>
            </html>
            """.formatted(
                adminName,
                username,
                temporaryPassword
        );
    }
}
