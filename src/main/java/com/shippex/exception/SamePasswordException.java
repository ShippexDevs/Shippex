package com.shippex.exception;

public class SamePasswordException extends RuntimeException{
    public SamePasswordException() {
        super("New password cannot be the same as the current password.");
    }
}
