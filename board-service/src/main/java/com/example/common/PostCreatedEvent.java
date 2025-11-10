package com.example.common;

import com.example.board_service.Post;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostCreatedEvent {
    private final Long postId;
    private final String author;

    public static PostCreatedEvent from(Post post) {
        return PostCreatedEvent.builder()
                .postId(post.getId())
                .author(post.getAuthor())
                .build();
    }

}
