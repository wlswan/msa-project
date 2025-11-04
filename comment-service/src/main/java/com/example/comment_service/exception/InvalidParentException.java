package com.example.comment_service.exception;

public class InvalidParentException extends CommentException {
    public InvalidParentException() {
        super(ErrorCode.INVALID_PARENT);
    }
}
