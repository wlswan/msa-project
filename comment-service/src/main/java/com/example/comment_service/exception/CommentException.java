package com.example.comment_service.exception;

public class CommentException extends RuntimeException{
    private final ErrorCode errorCode;

    public CommentException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
