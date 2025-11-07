package com.example.notification_service;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private String targetUsername;
    private String message;
    private CommentType type;
    private Long postId;
    private boolean read;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .targetUsername(notification.getTargetUsername())
                .message(notification.getMessage())
                .type(notification.getType())
                .postId(notification.getPostId())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
