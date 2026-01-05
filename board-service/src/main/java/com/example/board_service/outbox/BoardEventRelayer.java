package com.example.board_service.outbox;

import com.example.board_service.rabbitmq.PostEventProducer;
import com.example.common.PostCreatedEvent;
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
public class BoardEventRelayer {

    private final OutBoxRepository outboxRepository;
    private final PostEventProducer postEventProducer;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void relay() {
        List<Outbox> outboxes = outboxRepository.findAllByStatusOrderByIdAsc(OutboxStatus.PENDING);

        if (outboxes.isEmpty()) {
            return;
        }

        log.info("발송 대기 중인 이벤트 {}개 발견 -> 전송 시작", outboxes.size());

        for (Outbox outbox : outboxes) {
            try {
                PostCreatedEvent event = objectMapper.readValue(outbox.getPayload(), PostCreatedEvent.class);

                postEventProducer.send(event);

                outbox.markAsSent();
                
                log.debug("이벤트 전송 성공: outboxId={}, postId={}", outbox.getId(), outbox.getAggregateId());

            } catch (Exception e) {
                log.error("메시지 재발송 실패: outboxId={}", outbox.getId(), e);
            }
        }
    }
}