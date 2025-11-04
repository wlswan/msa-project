package com.example.comment_service.exception;

import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CommentException.class)
    public ProblemDetail handleAuth(CommentException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(e.getErrorCode().getStatus(), e.getMessage());
        problem.setTitle("요청 처리 중 오류가 발생했습니다.");
        problem.setProperty("errorCode", e.getErrorCode().name());
        return problem;
    }

}
