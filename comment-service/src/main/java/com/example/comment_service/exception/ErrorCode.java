package com.example.comment_service.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    COMMENT_NOT_FOUND("존재하지 않는 댓글입니다.", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_REQUEST("삭제 권한이 없습니다.", HttpStatus.FORBIDDEN),
    INVALID_PARENT("존재하지 않는 상위 댓글입니다.", HttpStatus.BAD_REQUEST);

    private final String message;
    private final HttpStatus status;

    ErrorCode(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
