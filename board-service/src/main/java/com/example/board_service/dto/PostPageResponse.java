package com.example.board_service.dto;

import lombok.Data;

import java.util.List;

@Data
public class PostPageResponse {
    private final List<PostResponse> postResponses;
    private final int page;                  // 현재 페이지 번호
    private final int size;                  // 페이지 크기
    private final long totalElements;        // 전체 댓글 수
    private final int totalPages;            // 총 페이지 수


    public PostPageResponse(List<PostResponse> postResponses, int page, int size, long totalElements) {
        this.postResponses = postResponses;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / size);    }
}
