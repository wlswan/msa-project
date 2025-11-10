package com.example.comment_service.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 댓글 서비스용 교환기
    public static final String COMMENT_EXCHANGE = "comment.exchange";

    // 게시글 서비스 관련 교환기 및 큐 설정
    public static final String POST_EXCHANGE = "post.exchange";
    public static final String POST_CREATED_QUEUE = "post.created.queue";
    public static final String POST_CREATED_ROUTING_KEY = "post.created";

    @Bean
    public TopicExchange postExchange() {
        return new TopicExchange(POST_EXCHANGE);
    }

    // 게시글 생성 이벤트 큐
    @Bean
    public Queue postCreatedQueue() {
        return QueueBuilder.durable(POST_CREATED_QUEUE).build();
    }

    // 교환기 ↔ 큐 바인딩
    @Bean
    public Binding postCreatedBinding(Queue postCreatedQueue, TopicExchange postExchange) {
        return BindingBuilder.bind(postCreatedQueue)
                .to(postExchange)
                .with(POST_CREATED_ROUTING_KEY);
    }

    // 댓글 교환기 (댓글 관련 용도)
    @Bean
    public TopicExchange commentExchange() {
        return new TopicExchange(COMMENT_EXCHANGE);
    }

    // JSON 직렬화 설정
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
