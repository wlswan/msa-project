package com.example.notification_service;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class  Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String targetUsername;
    private CommentType type;
    private Long postId;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;


}
