package com.example.comment_service.rabbitmq;

import com.example.common.CommentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentEventProducer {

    private final RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE_NAME = "comment.exchange";
    private static final String CREATED_ROUTING_KEY = "comment.created";

    /**
     * Relayer에서 호출 - 예외 발생 시 던져서 재시도 가능하게
     */
    public void sendDirect(CommentEvent event) throws AmqpException {
        log.info("RabbitMQ 전송: commentId={}", event.getCommentId());
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, CREATED_ROUTING_KEY, event);
        log.info("RabbitMQ 전송 성공");
    }
}
