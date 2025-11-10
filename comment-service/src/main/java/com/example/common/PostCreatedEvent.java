package com.example.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostCreatedEvent {
    private final Long postId;
    private final String author;


}
