package com.example.comment_service.rabbitmq;

import com.example.common.CommentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentEventProducer {
    private final RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE_NAME = "comment.exchange";
    private static final String CREATED_ROUTING_KEY = "comment.created";
    public void send(CommentEvent event) {
        try {
            log.info("RabbitMQ 전송 시도: {}", event);

            rabbitTemplate.convertAndSend(EXCHANGE_NAME, CREATED_ROUTING_KEY, event);

            log.info("RabbitMQ 전송 성공! exchange={}, routingKey={}", EXCHANGE_NAME, CREATED_ROUTING_KEY);
        } catch (Exception ex) {
            log.error("RabbitMQ 전송 실패: {}", ex.getMessage());
        }
    }
}
