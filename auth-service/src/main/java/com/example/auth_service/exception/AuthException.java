package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;

public abstract class AuthException extends RuntimeException {
    private final ErrorCode errorCode;

    protected AuthException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
