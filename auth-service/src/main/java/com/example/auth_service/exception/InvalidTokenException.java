package com.example.auth_service.exception;

public class InvalidTokenException extends AuthException {
    public InvalidTokenException() {
        super(ErrorCode.INVALID_TOKEN);
    }
}