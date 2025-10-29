package com.example.auth_service.exception;

public class MissingTokenException extends AuthException {
    public MissingTokenException() {
        super(ErrorCode.MISSING_TOKEN);
    }
}