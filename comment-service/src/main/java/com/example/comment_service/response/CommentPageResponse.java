package com.example.comment_service.response;

import lombok.Getter;

import java.util.List;

@Getter
public class CommentPageResponse {
    private final List<CommentResponse> content;     // 현재 페이지 댓글 리스트
    private final int page;                  // 현재 페이지 번호
    private final int size;                  // 페이지 크기
    private final long totalElements;        // 전체 댓글 수
    private final int totalPages;            // 총 페이지 수
    private final boolean last;              // 마지막 페이지 여부

    public CommentPageResponse(List<CommentResponse> content, int page, int size, long totalElements) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / size);
        this.last = (page + 1) * size >= totalElements;
    }
}

