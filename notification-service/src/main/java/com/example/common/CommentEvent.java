package com.example.notification_service;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentEvent {
    private Long postId;
    private Long commentId;
    private String writer;
    private String targetUser;
    private String content;
    private CommentType type;
}
