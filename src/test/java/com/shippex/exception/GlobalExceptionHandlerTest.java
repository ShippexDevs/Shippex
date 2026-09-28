package com.shippex.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import jakarta.validation.ConstraintViolationException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void otpErrorsBecomeBadRequest() {
        var response = handler.handleOtpException(new OtpException("OTP expired"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("OTP expired", response.getBody().getMessage());
    }

    @Test
    void validationErrorsReturnFirstFieldMessage() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(new FieldError("request", "name", "Name required")));
        var response = handler.handleValidationException(exception);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Name required", response.getBody().getMessage());
    }

    @Test
    void constraintViolationsBecomeBadRequest() {
        var response = handler.handleConstraintViolation(new ConstraintViolationException("invalid value", Set.of()));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("invalid value", response.getBody().getMessage());
    }

    @Test
    void notFoundExceptionsBecomeNotFound() {
        assertEquals(HttpStatus.NOT_FOUND,
                handler.handleNotFoundException(new OrderNotFoundException("missing order")).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND,
                handler.handleNotFoundException(new ProductNotFoundException("missing product")).getStatusCode());
    }

    @Test
    void invalidRequestsBecomeBadRequest() {
        assertEquals(HttpStatus.BAD_REQUEST,
                handler.handleBadRequestException(new InvalidOrderStatusException("transition rejected")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST,
                handler.handleBadRequestException(new InsufficientStockException("out of stock")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST,
                handler.handleBadRequestException(new IllegalArgumentException("bad input")).getStatusCode());
    }

    @Test
    void unexpectedErrorsReturnSanitizedInternalError() {
        var response = handler.handleGenericException(new IllegalStateException("database secret"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Something went wrong. Please try again later.", response.getBody().getMessage());
        assertFalse(response.getBody().getMessage().contains("database secret"));
    }
}
