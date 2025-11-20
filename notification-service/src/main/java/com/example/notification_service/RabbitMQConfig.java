package com.example.notification_service;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;

public class RabbitMQConfig {
    // 게시글 서비스 관련 교환기 및 큐 설정
    public static final String COMMENT = "comment.exchange";
    public static final String COMMENT_CREATED_QUEUE = "comment.created.queue";
    public static final String COMMENT_CREATED_ROUTING_KEY = "comment.created";


    // 게시글 생성 이벤트 큐
    @Bean
    public Queue commentCreatedQueue() {
        return QueueBuilder.durable(COMMENT_CREATED_QUEUE).build();
    }

    // 교환기 ↔ 큐 바인딩
    @Bean
    public Binding postCreatedBinding(Queue postCreatedQueue, TopicExchange postExchange) {
        return BindingBuilder.bind(postCreatedQueue)
                .to(postExchange)
                .with(COMMENT_CREATED_ROUTING_KEY);
    }

    // JSON 직렬화 설정
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }


}
