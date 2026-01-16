package com.example.comment_service;

import com.example.comment_service.outbox.CommentOutbox;
import com.example.comment_service.outbox.CommentOutboxRepository;
import com.example.common.CommentEvent;
import com.example.common.CommentType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentEventListener {

    private final CommentOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleCommentSaved(CommentSavedEvent event) {
        Comment comment = event.getComment();
        String targetUser = event.getTargetUser();



        CommentType commentType = event.getTargetType() == NotificationTargetType.PARENT_AUTHOR
                ? CommentType.REPLY
                : CommentType.COMMENT;

        CommentEvent mqEvent = CommentEvent.builder()
                .postId(comment.getPostId())
                .commentId(comment.getId())
                .writer(comment.getAuthor())
                .content(comment.getContent())
                .targetUser(targetUser)
                .type(commentType)
                .build();

        saveToOutbox(mqEvent);
    }

    private void saveToOutbox(CommentEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            CommentOutbox outbox = CommentOutbox.builder()
                    .commentId(event.getCommentId())
                    .eventType("COMMENT_CREATED")
                    .payload(payload)
                    .build();
            outboxRepository.save(outbox);
            log.debug("Outbox 저장 완료: commentId={}", event.getCommentId());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("이벤트 직렬화 실패", e);
        }
    }
}
