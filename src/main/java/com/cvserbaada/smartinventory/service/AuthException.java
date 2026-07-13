package com.cvserbaada.smartinventory.service;

public class AuthException extends Exception {
    private final AuthErrorType errorType;

    public AuthException(AuthErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public AuthErrorType getErrorType() {
        return errorType;
    }
}
