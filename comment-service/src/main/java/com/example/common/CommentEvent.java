package com.example.common;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentEvent {
    private Long postId;
    private Long commentId;
    private String writer;
    private String targetUser;
    private String content;
    private CommentType type;
}
