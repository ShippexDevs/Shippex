package com.shippex.exception;

public class AdminNotFoundException extends RuntimeException {

    public AdminNotFoundException(String adminId) {
        super("Admin not found with id : " + adminId);
    }
}
