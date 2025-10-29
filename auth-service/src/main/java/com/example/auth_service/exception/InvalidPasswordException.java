package com.example.auth_service.exception;

public class InvalidPasswordException extends AuthException {
    public InvalidPasswordException() {
        super(ErrorCode.INVALID_PASSWORD);
    }
}