package com.example.comment_service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CommentSavedEvent {
    private final Comment comment;
    private final Long parentId;
    private final String parentAuthor;  // 답글인 경우 부모 댓글 작성자
}
