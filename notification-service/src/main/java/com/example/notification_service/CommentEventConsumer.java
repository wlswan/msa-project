package com.example.notification_service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "comment-events", groupId = "notification-group")
    public void consume(CommentEvent event) {
        log.info("댓글 이벤트 수신: {}", event);
        notificationService.createNotification(event);
    }
}
