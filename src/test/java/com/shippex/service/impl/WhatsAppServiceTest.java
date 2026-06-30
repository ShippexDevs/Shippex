package com.shippex.service.impl;

import com.shippex.dto.notification.NotificationRequest;
import com.shippex.dto.whatsapp.WhatsAppApiResponse;
import com.shippex.dto.whatsapp.WhatsAppTextMessageRequest;
import com.shippex.exception.WhatsAppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WhatsAppServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private WhatsAppService whatsAppService;

    private NotificationRequest request;

    @BeforeEach
    void setUp() {

        request = new NotificationRequest(
                "+919876543210",
                "Hello World");
    }

    // ============================================================
    // Happy Path
    // ============================================================

    @Test
    void sendTextMessage_shouldSendSuccessfully() {

        WhatsAppApiResponse.Message message =
                new WhatsAppApiResponse.Message();

        message.setId("wamid.123");

        WhatsAppApiResponse response =
                new WhatsAppApiResponse();

        response.setMessages(List.of(message));

        when(restClient.post())
                .thenReturn(requestBodyUriSpec);

        doReturn(requestBodySpec)
                .when(requestBodyUriSpec)
                .body(any(WhatsAppTextMessageRequest.class));

        when(requestBodySpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.onStatus(any(), any()))
                .thenReturn(responseSpec);

        when(responseSpec.body(WhatsAppApiResponse.class))
                .thenReturn(response);

        String id =
                whatsAppService.sendTextMessage(request);

        assertThat(id).isEqualTo("wamid.123");
    }

    @Test
    void sendTextMessage_shouldBuildRequestCorrectly() {

        WhatsAppApiResponse.Message message =
                new WhatsAppApiResponse.Message();
        message.setId("123");

        WhatsAppApiResponse response =
                new WhatsAppApiResponse();
        response.setMessages(List.of(message));

        when(restClient.post())
                .thenReturn(requestBodyUriSpec);

        doReturn(requestBodySpec)
                .when(requestBodyUriSpec)
                .body(any(WhatsAppTextMessageRequest.class));

        when(requestBodySpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.onStatus(any(), any()))
                .thenReturn(responseSpec);

        when(responseSpec.body(WhatsAppApiResponse.class))
                .thenReturn(response);

        ArgumentCaptor<WhatsAppTextMessageRequest> captor =
                ArgumentCaptor.forClass(WhatsAppTextMessageRequest.class);

        whatsAppService.sendTextMessage(request);

        verify(requestBodyUriSpec).body(captor.capture());

        WhatsAppTextMessageRequest body = captor.getValue();

        assertThat(body.getMessagingProduct())
                .isEqualTo("whatsapp");

        assertThat(body.getType())
                .isEqualTo("text");

        assertThat(body.getTo())
                .isEqualTo("919876543210");

        assertThat(body.getText().getBody())
                .isEqualTo("Hello World");
    }

    @Test
    void sendTextMessage_shouldRemovePlusFromPhoneNumber() {

        request.setRecipient("+91 9876543210");

        WhatsAppApiResponse.Message message =
                new WhatsAppApiResponse.Message();
        message.setId("abc");

        WhatsAppApiResponse response =
                new WhatsAppApiResponse();
        response.setMessages(List.of(message));

        when(restClient.post())
                .thenReturn(requestBodyUriSpec);

        doReturn(requestBodySpec)
                .when(requestBodyUriSpec)
                .body(any(WhatsAppTextMessageRequest.class));

        when(requestBodySpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.onStatus(any(), any()))
                .thenReturn(responseSpec);

        when(responseSpec.body(WhatsAppApiResponse.class))
                .thenReturn(response);

        ArgumentCaptor<WhatsAppTextMessageRequest> captor =
                ArgumentCaptor.forClass(WhatsAppTextMessageRequest.class);

        whatsAppService.sendTextMessage(request);

        verify(requestBodyUriSpec).body(captor.capture());

        assertThat(captor.getValue().getTo())
                .isEqualTo("919876543210");
    }

    @Test
    void sendTextMessage_shouldReturnMessageId() {

        WhatsAppApiResponse.Message message =
                new WhatsAppApiResponse.Message();
        message.setId("wamid.98765");

        WhatsAppApiResponse response =
                new WhatsAppApiResponse();
        response.setMessages(List.of(message));

        when(restClient.post())
                .thenReturn(requestBodyUriSpec);

        doReturn(requestBodySpec)
                .when(requestBodyUriSpec)
                .body(any(WhatsAppTextMessageRequest.class));

        when(requestBodySpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.onStatus(any(), any()))
                .thenReturn(responseSpec);

        when(responseSpec.body(WhatsAppApiResponse.class))
                .thenReturn(response);

        String id = whatsAppService.sendTextMessage(request);

        assertThat(id)
                .isEqualTo("wamid.98765");
    }

    @Test
    void sendTextMessage_shouldReturnFirstMessageIdWhenMultipleMessagesExist() {

        WhatsAppApiResponse.Message first =
                new WhatsAppApiResponse.Message();
        first.setId("first-id");

        WhatsAppApiResponse.Message second =
                new WhatsAppApiResponse.Message();
        second.setId("second-id");

        WhatsAppApiResponse response =
                new WhatsAppApiResponse();
        response.setMessages(List.of(first, second));

        when(restClient.post())
                .thenReturn(requestBodyUriSpec);

        doReturn(requestBodySpec)
                .when(requestBodyUriSpec)
                .body(any(WhatsAppTextMessageRequest.class));

        when(requestBodySpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.onStatus(any(), any()))
                .thenReturn(responseSpec);

        when(responseSpec.body(WhatsAppApiResponse.class))
                .thenReturn(response);

        String id = whatsAppService.sendTextMessage(request);

        assertThat(id)
                .isEqualTo("first-id");
    }

    @Test
    void sendTextMessage_shouldIgnoreSpacesInPhoneNumber() {

        request.setRecipient("+91 98765 43210");

        WhatsAppApiResponse.Message message =
                new WhatsAppApiResponse.Message();
        message.setId("id");

        WhatsAppApiResponse response =
                new WhatsAppApiResponse();
        response.setMessages(List.of(message));

        when(restClient.post())
                .thenReturn(requestBodyUriSpec);

        doReturn(requestBodySpec)
                .when(requestBodyUriSpec)
                .body(any(WhatsAppTextMessageRequest.class));

        when(requestBodySpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.onStatus(any(), any()))
                .thenReturn(responseSpec);

        when(responseSpec.body(WhatsAppApiResponse.class))
                .thenReturn(response);

        ArgumentCaptor<WhatsAppTextMessageRequest> captor =
                ArgumentCaptor.forClass(WhatsAppTextMessageRequest.class);

        whatsAppService.sendTextMessage(request);

        verify(requestBodyUriSpec).body(captor.capture());

        assertThat(captor.getValue().getTo())
                .isEqualTo("919876543210");
    }

    // ============================================================
    // Failure Scenarios
    // ============================================================

    @Test
    void sendTextMessage_shouldThrowWhenResponseIsNull() {

        when(restClient.post())
                .thenReturn(requestBodyUriSpec);

        doReturn(requestBodySpec)
                .when(requestBodyUriSpec)
                .body(any(WhatsAppTextMessageRequest.class));

        when(requestBodySpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.onStatus(any(), any()))
                .thenReturn(responseSpec);

        when(responseSpec.body(WhatsAppApiResponse.class))
                .thenReturn(null);

        assertThatThrownBy(() ->
                whatsAppService.sendTextMessage(request))
                .isInstanceOf(WhatsAppException.class)
                .hasMessage("Meta API returned an empty response.");
    }

    @Test
    void sendTextMessage_shouldThrowWhenMessagesAreNull() {

        WhatsAppApiResponse response =
                new WhatsAppApiResponse();

        response.setMessages(null);

        when(restClient.post())
                .thenReturn(requestBodyUriSpec);

        doReturn(requestBodySpec)
                .when(requestBodyUriSpec)
                .body(any(WhatsAppTextMessageRequest.class));

        when(requestBodySpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.onStatus(any(), any()))
                .thenReturn(responseSpec);

        when(responseSpec.body(WhatsAppApiResponse.class))
                .thenReturn(response);

        assertThatThrownBy(() ->
                whatsAppService.sendTextMessage(request))
                .isInstanceOf(WhatsAppException.class)
                .hasMessage("Meta API returned an empty response.");
    }

    @Test
    void sendTextMessage_shouldThrowWhenMessagesListIsEmpty() {

        WhatsAppApiResponse response =
                new WhatsAppApiResponse();

        response.setMessages(List.of());

        when(restClient.post())
                .thenReturn(requestBodyUriSpec);

        doReturn(requestBodySpec)
                .when(requestBodyUriSpec)
                .body(any(WhatsAppTextMessageRequest.class));

        when(requestBodySpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.onStatus(any(), any()))
                .thenReturn(responseSpec);

        when(responseSpec.body(WhatsAppApiResponse.class))
                .thenReturn(response);

        assertThatThrownBy(() ->
                whatsAppService.sendTextMessage(request))
                .isInstanceOf(WhatsAppException.class)
                .hasMessage("Meta API returned an empty response.");
    }

    @Test
    void sendTextMessage_shouldRethrowWhatsAppException() {

        when(restClient.post())
                .thenReturn(requestBodyUriSpec);

        doReturn(requestBodySpec)
                .when(requestBodyUriSpec)
                .body(any(WhatsAppTextMessageRequest.class));

        when(requestBodySpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.onStatus(any(), any()))
                .thenThrow(new WhatsAppException("Meta API Error"));

        assertThatThrownBy(() ->
                whatsAppService.sendTextMessage(request))
                .isInstanceOf(WhatsAppException.class)
                .hasMessage("Meta API Error");
    }

    @Test
    void sendTextMessage_shouldWrapRestClientException() {

        when(restClient.post())
                .thenReturn(requestBodyUriSpec);

        doReturn(requestBodySpec)
                .when(requestBodyUriSpec)
                .body(any(WhatsAppTextMessageRequest.class));

        when(requestBodySpec.retrieve())
                .thenThrow(new RestClientException("Connection refused"));

        assertThatThrownBy(() ->
                whatsAppService.sendTextMessage(request))
                .isInstanceOf(WhatsAppException.class)
                .hasMessage("Unable to communicate with Meta Cloud API.")
                .hasCauseInstanceOf(RestClientException.class);
    }

    @Test
    void sendTextMessage_shouldWrapUnexpectedException() {

        when(restClient.post())
                .thenReturn(requestBodyUriSpec);

        doReturn(requestBodySpec)
                .when(requestBodyUriSpec)
                .body(any(WhatsAppTextMessageRequest.class));

        when(requestBodySpec.retrieve())
                .thenThrow(new RuntimeException("Unexpected"));

        assertThatThrownBy(() ->
                whatsAppService.sendTextMessage(request))
                .isInstanceOf(WhatsAppException.class)
                .hasMessage("Unexpected error occurred while sending WhatsApp message.")
                .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    void sendTextMessage_shouldThrowWhenPhoneNumberIsNull() {

        request.setRecipient(null);

        assertThatThrownBy(() ->
                whatsAppService.sendTextMessage(request))
                .isInstanceOf(WhatsAppException.class)
                .hasMessage("Phone number cannot be null.");

        verifyNoInteractions(restClient);
    }

    @Test
    void sendTextMessage_shouldPreserveOriginalWhatsAppException() {

        WhatsAppException exception =
                new WhatsAppException("Already handled");

        when(restClient.post())
                .thenReturn(requestBodyUriSpec);

        doReturn(requestBodySpec)
                .when(requestBodyUriSpec)
                .body(any(WhatsAppTextMessageRequest.class));

        when(requestBodySpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.onStatus(any(), any()))
                .thenReturn(responseSpec);

        when(responseSpec.body(WhatsAppApiResponse.class))
                .thenThrow(exception);

        assertThatThrownBy(() ->
                whatsAppService.sendTextMessage(request))
                .isSameAs(exception);
    }

    // ============================================================
    // Edge Cases & Regression Tests
    // ============================================================

    @Test
    void sendTextMessage_shouldReturnNullMessageId_whenMetaReturnsNullId() {

        WhatsAppApiResponse.Message message =
                new WhatsAppApiResponse.Message();
        message.setId(null);

        WhatsAppApiResponse response =
                new WhatsAppApiResponse();
        response.setMessages(List.of(message));

        when(restClient.post())
                .thenReturn(requestBodyUriSpec);

        doReturn(requestBodySpec)
                .when(requestBodyUriSpec)
                .body(any(WhatsAppTextMessageRequest.class));

        when(requestBodySpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.onStatus(any(), any()))
                .thenReturn(responseSpec);

        when(responseSpec.body(WhatsAppApiResponse.class))
                .thenReturn(response);

        String id = whatsAppService.sendTextMessage(request);

        assertThat(id).isNull();
    }

    @Test
    void sendTextMessage_shouldAllowEmptyMessageBody() {

        request.setMessage("");

        WhatsAppApiResponse.Message message =
                new WhatsAppApiResponse.Message();
        message.setId("msg123");

        WhatsAppApiResponse response =
                new WhatsAppApiResponse();
        response.setMessages(List.of(message));

        when(restClient.post())
                .thenReturn(requestBodyUriSpec);

        doReturn(requestBodySpec)
                .when(requestBodyUriSpec)
                .body(any(WhatsAppTextMessageRequest.class));

        when(requestBodySpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.onStatus(any(), any()))
                .thenReturn(responseSpec);

        when(responseSpec.body(WhatsAppApiResponse.class))
                .thenReturn(response);

        ArgumentCaptor<WhatsAppTextMessageRequest> captor =
                ArgumentCaptor.forClass(WhatsAppTextMessageRequest.class);

        whatsAppService.sendTextMessage(request);

        verify(requestBodyUriSpec)
                .body(captor.capture());

        assertThat(captor.getValue().getText().getBody())
                .isEmpty();
    }

    @Test
    void sendTextMessage_shouldAllowSpecialCharactersInMessage() {

        request.setMessage("OTP: 123456\n₹100\nالسلام عليكم");

        WhatsAppApiResponse.Message message =
                new WhatsAppApiResponse.Message();
        message.setId("id");

        WhatsAppApiResponse response =
                new WhatsAppApiResponse();
        response.setMessages(List.of(message));

        when(restClient.post())
                .thenReturn(requestBodyUriSpec);

        doReturn(requestBodySpec)
                .when(requestBodyUriSpec)
                .body(any(WhatsAppTextMessageRequest.class));

        when(requestBodySpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.onStatus(any(), any()))
                .thenReturn(responseSpec);

        when(responseSpec.body(WhatsAppApiResponse.class))
                .thenReturn(response);

        ArgumentCaptor<WhatsAppTextMessageRequest> captor =
                ArgumentCaptor.forClass(WhatsAppTextMessageRequest.class);

        whatsAppService.sendTextMessage(request);

        verify(requestBodyUriSpec)
                .body(captor.capture());

        assertThat(captor.getValue()
                .getText()
                .getBody())
                .isEqualTo("OTP: 123456\n₹100\nالسلام عليكم");
    }

    @Test
    void sendTextMessage_shouldRemoveOnlySpacesAndPlusSignFromPhoneNumber() {

        request.setRecipient("+91 98765 43210");

        WhatsAppApiResponse.Message message =
                new WhatsAppApiResponse.Message();
        message.setId("id");

        WhatsAppApiResponse response =
                new WhatsAppApiResponse();
        response.setMessages(List.of(message));

        when(restClient.post())
                .thenReturn(requestBodyUriSpec);

        doReturn(requestBodySpec)
                .when(requestBodyUriSpec)
                .body(any(WhatsAppTextMessageRequest.class));

        when(requestBodySpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.onStatus(any(), any()))
                .thenReturn(responseSpec);

        when(responseSpec.body(WhatsAppApiResponse.class))
                .thenReturn(response);

        ArgumentCaptor<WhatsAppTextMessageRequest> captor =
                ArgumentCaptor.forClass(WhatsAppTextMessageRequest.class);

        whatsAppService.sendTextMessage(request);

        verify(requestBodyUriSpec)
                .body(captor.capture());

        assertThat(captor.getValue().getTo())
                .isEqualTo("919876543210");
    }

    @Test
    void sendTextMessage_shouldInvokeRestClientOnlyOnce() {

        WhatsAppApiResponse.Message message =
                new WhatsAppApiResponse.Message();
        message.setId("id");

        WhatsAppApiResponse response =
                new WhatsAppApiResponse();
        response.setMessages(List.of(message));

        when(restClient.post())
                .thenReturn(requestBodyUriSpec);

        doReturn(requestBodySpec)
                .when(requestBodyUriSpec)
                .body(any(WhatsAppTextMessageRequest.class));

        when(requestBodySpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.onStatus(any(), any()))
                .thenReturn(responseSpec);

        when(responseSpec.body(WhatsAppApiResponse.class))
                .thenReturn(response);

        whatsAppService.sendTextMessage(request);

        verify(restClient, times(1)).post();
        verify(requestBodyUriSpec, times(1))
                .body(any(WhatsAppTextMessageRequest.class));
        verify(requestBodySpec, times(1))
                .retrieve();
        verify(responseSpec, times(1))
                .body(WhatsAppApiResponse.class);
    }
}