package com.example.comment_service.exception;

public class CommentNotFoundException extends CommentException {
    public CommentNotFoundException() {
        super(ErrorCode.COMMENT_NOT_FOUND);
    }
}
