package com.example.board_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostRequest {
    private String title;
    private String content;
}
