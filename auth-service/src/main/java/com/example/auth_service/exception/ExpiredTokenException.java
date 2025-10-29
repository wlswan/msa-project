package com.example.auth_service.exception;

public class ExpiredTokenException extends AuthException {
    public ExpiredTokenException() {
        super(ErrorCode.EXPIRED_TOKEN);
    }
}