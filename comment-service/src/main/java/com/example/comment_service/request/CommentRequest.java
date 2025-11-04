package com.example.comment_service.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentRequest {
    private String content;
    private Long parentId;
}
