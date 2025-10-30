package com.example.board_service.exception;

public class UnAuthorizedRequestException extends PostException{
    public UnAuthorizedRequestException() {
        super(ErrorCode.UNAUTHORIZED_REQUEST);
    }
}
