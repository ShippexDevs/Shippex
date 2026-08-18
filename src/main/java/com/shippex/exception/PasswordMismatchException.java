package com.shippex.exception;

public class PasswordMismatchException extends RuntimeException{
    public PasswordMismatchException() {
        super("New password and confirm password do not match.");
    }
}
