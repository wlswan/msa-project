package com.example.board_service.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostRequest {
    private String title;
    private String content;
}
