package com.example.comment_service.rabbitmq;

import com.example.common.PostCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;


@Slf4j
@Component
@RequiredArgsConstructor
public class PostEventConsumer {

    private final RedisTemplate<String, String> redisTemplate;

    @RabbitListener(queues = "post.created.queue")
    public void handlePostCreated(PostCreatedEvent event) {
        log.info("게시글 생성 이벤트 수신: {}", event);

        String key = "post:" + event.getPostId();
        String author = event.getAuthor();
        redisTemplate.opsForValue().set(key,author, Duration.ofHours(1));
    }

}
