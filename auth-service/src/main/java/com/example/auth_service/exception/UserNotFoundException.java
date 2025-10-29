package com.example.auth_service.exception;

public class UserNotFoundException extends AuthException {
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}
