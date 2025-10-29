package com.example.auth_service.exception;

public class DuplicateUsernameException extends AuthException {
    public DuplicateUsernameException() {
        super(ErrorCode.DUPLICATE_USERNAME);
    }
}