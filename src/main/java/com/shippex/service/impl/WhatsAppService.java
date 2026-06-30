package com.shippex.service.impl;

import com.shippex.dto.notification.NotificationRequest;
import com.shippex.dto.whatsapp.WhatsAppApiResponse;
import com.shippex.dto.whatsapp.WhatsAppText;
import com.shippex.dto.whatsapp.WhatsAppTextMessageRequest;
import com.shippex.exception.WhatsAppException;
import com.shippex.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class WhatsAppService implements NotificationService {

    private static final Logger logger =
            LoggerFactory.getLogger(WhatsAppService.class);

    private final RestClient restClient;

    @Override
    public String sendTextMessage(NotificationRequest request) {

        logger.info("Preparing WhatsApp message for recipient [{}]",
                request.getRecipient());

        WhatsAppTextMessageRequest body = buildRequest(request);

        try {

            WhatsAppApiResponse response = restClient.post()
                    .body(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError,
                            (req, res) -> {

                                throw new WhatsAppException(
                                        "Meta WhatsApp API returned HTTP "
                                                + res.getStatusCode().value());

                            })
                    .body(WhatsAppApiResponse.class);

            if (response == null ||
                    response.getMessages() == null ||
                    response.getMessages().isEmpty()) {

                logger.error("Meta API returned an empty response.");

                throw new WhatsAppException(
                        "Meta API returned an empty response.");
            }

            String messageId =
                    response.getMessages().getFirst().getId();

            logger.info("WhatsApp message sent successfully. Message Id={}",
                    messageId);

            return messageId;

        }
        catch (WhatsAppException ex) {

            logger.error("WhatsApp API Error : {}", ex.getMessage());

            throw ex;

        }
        catch (RestClientException ex) {

            logger.error("Unable to communicate with Meta Cloud API", ex);

            throw new WhatsAppException(
                    "Unable to communicate with Meta Cloud API.",
                    ex);

        }
        catch (Exception ex) {

            logger.error("Unexpected error while sending WhatsApp message.",
                    ex);

            throw new WhatsAppException(
                    "Unexpected error occurred while sending WhatsApp message.",
                    ex);

        }

    }

    /**
     * Builds the request body expected by Meta Cloud API.
     */
    private WhatsAppTextMessageRequest buildRequest(
            NotificationRequest request) {

        WhatsAppTextMessageRequest body =
                new WhatsAppTextMessageRequest();

        body.setTo(formatPhoneNumber(request.getRecipient()));

        body.setText(
                new WhatsAppText(
                        request.getMessage()));

        return body;

    }

    /**
     * Meta expects the number without '+'.
     */
    private String formatPhoneNumber(String phone) {

        if (phone == null) {
            throw new WhatsAppException("Phone number cannot be null.");
        }

        return phone.replace("+", "")
                .replace(" ", "");

    }

}