package com.example.board_service.exception;

public class PostNotFoundException extends PostException{
    public PostNotFoundException() {
        super(ErrorCode.POST_NOT_FOUND);
    }
}
