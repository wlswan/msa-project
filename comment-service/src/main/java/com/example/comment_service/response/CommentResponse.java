package com.example.comment_service.response;

import com.example.comment_service.Comment;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentResponse {
    private Long id;
    private Long postId;
    private String author;
    private Boolean deleted;
    private String content;
    private Long parentId;

    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .author(comment.getAuthor())
                .deleted(comment.getDeleted())
                .content(comment.getContent())
                .parentId(comment.getParentId())
                .build();
    }
}
