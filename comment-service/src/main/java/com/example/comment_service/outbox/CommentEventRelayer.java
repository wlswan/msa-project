package com.example.comment_service.outbox;

import com.example.comment_service.rabbitmq.CommentEventProducer;
import com.example.common.CommentEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentEventRelayer {

    private final CommentOutboxRepository outboxRepository;
    private final CommentEventProducer commentEventProducer;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void relay() {
        List<CommentOutbox> outboxes = outboxRepository.findAllByStatusOrderByIdAsc(OutboxStatus.PENDING);

        if (outboxes.isEmpty()) {
            return;
        }

        log.info("발송 대기 중인 댓글 이벤트 {}개 발견", outboxes.size());

        for (CommentOutbox outbox : outboxes) {
            try {
                CommentEvent event = objectMapper.readValue(outbox.getPayload(), CommentEvent.class);
                commentEventProducer.sendDirect(event);
                outbox.markAsSent();
                log.debug("이벤트 전송 성공: outboxId={}, commentId={}", outbox.getId(), outbox.getCommentId());
            } catch (Exception e) {
                log.error("이벤트 전송 실패 (재시도 예정): outboxId={}, error={}", outbox.getId(), e.getMessage());
            }
        }
    }
}