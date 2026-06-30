package com.shippex.config;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "whatsapp")
public class WhatsAppProperties {

    private static final Logger logger =
            LoggerFactory.getLogger(WhatsAppProperties.class);

    /**
     * Example: v23.0
     */
    @NotBlank(message = "WhatsApp API version must not be blank.")
    private String apiVersion;

    /**
     * Meta Cloud API Access Token
     */
    @NotBlank(message = "WhatsApp access token must not be blank.")
    private String accessToken;

    /**
     * Meta Phone Number ID
     */
    @NotBlank(message = "WhatsApp phone number ID must not be blank.")
    private String phoneNumberId;

    /**
     * Cached Base URL
     */
    private String baseUrl;

    @PostConstruct
    public void initialize() {

        logger.info("Loading WhatsApp Cloud API configuration...");

        baseUrl = String.format(
                "https://graph.facebook.com/%s/%s/messages",
                apiVersion,
                phoneNumberId
        );

        logger.info("WhatsApp Cloud API configuration loaded successfully.");
        logger.info("Using WhatsApp Cloud API Version : {}", apiVersion);
    }
}