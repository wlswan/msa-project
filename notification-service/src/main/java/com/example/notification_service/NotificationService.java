package com.example.notification_service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public List<NotificationResponse> getNotifications(String username) {
        return notificationRepository.findByTargetUsernameOrderByCreatedAtDesc(username)
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    public void createNotification(CommentEvent event) {
        String message = buildMessage(event);

        Notification notification = Notification.builder()
                .targetUsername(event.getTargetUser())
                .type(event.getType())
                .postId(event.getPostId())
                .message(message)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);

    }


    private String buildMessage(CommentEvent event) {
        return switch(event.getType()){
            case COMMENT -> String.format("%s님이 내 게시글에 댓글을 남겼습니다.", event.getWriter());
            case REPLY -> String.format("%s님이 내 댓글에 답글을 남겼습니다.", event.getWriter());
        };
    }
}
