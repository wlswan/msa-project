package com.example.comment_service.exception;

public class UnauthorizedRequestException extends CommentException {
    public UnauthorizedRequestException() {
        super(ErrorCode.UNAUTHORIZED_REQUEST);
    }
}
