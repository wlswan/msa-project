package com.example.comment_service;

import com.example.comment_service.rabbitmq.CommentEventProducer;
import com.example.comment_service.rabbitmq.PostClient;
import com.example.common.CommentEvent;
import com.example.common.CommentType;
import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentEventListener {

    private final PostAuthorProvider postAuthorProvider;
    private final CommentEventProducer commentEventProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleCommentSaved(CommentSavedEvent event) {
        Long postId = event.getComment().getPostId();

        String targetUser = postAuthorProvider.getAuthorWithCache(postId);

        if (targetUser == null) {
            log.warn("작성자 정보를 가져올 수 없음. postId={}", postId);
            return;
        }
        CommentEvent mqEvent = CommentEvent.builder()
                .postId(postId)
                .commentId(event.getComment().getId())
                .writer(event.getComment().getAuthor())
                .content(event.getComment().getContent())
                .targetUser(targetUser)
                .type(event.getParentId() == null ? CommentType.COMMENT : CommentType.REPLY)
                .build();

        commentEventProducer.send(mqEvent);
    }

}
