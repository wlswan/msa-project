package com.example.board_service.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    POST_NOT_FOUND("존재하지 않는 게시글입니다.",HttpStatus.NOT_FOUND),
    UNAUTHORIZED_REQUEST("권한이 없습니다.", HttpStatus.FORBIDDEN);

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
