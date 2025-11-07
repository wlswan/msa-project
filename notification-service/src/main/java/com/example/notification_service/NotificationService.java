package com.example.notification_service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public List<NotificationResponse> getNotifications(String username) {
        return notificationRepository.findByReceiverOrderByCreatedAtDesc(username)
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    public void createNotification(CommentEvent event) {
        String message = buildMessage(event);

        Notification notification = Notification.builder()
                .receiver(event.getTargetUser())
                .sender(event.getWriter())
                .type(event.getType())
                .postId(event.getPostId())
                .message(message)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);

        try {
            messagingTemplate.convertAndSendToUser(
                    event.getTargetUser(),
                    "/queue/notifications",
                    NotificationResponse.from(notification)
            );
            log.info("📢 실시간 알림 전송 완료: {}", event.getTargetUser());
        } catch (Exception e) {
            log.error("❌ WebSocket 전송 실패: {}", e.getMessage());
        }

    }



    private String buildMessage(CommentEvent event) {
        return switch(event.getType()){
            case COMMENT -> String.format("%s님이 내 게시글에 댓글을 남겼습니다.", event.getWriter());
            case REPLY -> String.format("%s님이 내 댓글에 답글을 남겼습니다.", event.getWriter());
        };
    }
}
