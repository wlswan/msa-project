package com.example.notification_service;

import com.example.common.CommentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    uniqueConstraints = @UniqueConstraint(columnNames = "commentId")
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Long commentId;  // 멱등성 보장용

    private String receiver;
    private String sender;
    private CommentType type;
    private Long postId;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
}
