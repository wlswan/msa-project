package com.example.comment_service;

import com.example.comment_service.rabbitmq.CommentEventProducer;
import com.example.comment_service.rabbitmq.PostClient;
import com.example.common.CommentEvent;
import com.example.common.CommentType;
import feign.RetryableException;
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

    private final PostClient postClient;
    private final CommentEventProducer commentEventProducer;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedissonClient redissonClient;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(
            retryFor = { RetryableException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    @Async
    // 1. 데이터가 없는 404일때는 그냥 null로 이후 알림 처리 x
    // 2. 네트워크 오류일때는 재시도 로직 이용
    public void handleCommentSaved(CommentSavedEvent event) {
        Long postId = event.getComment().getPostId();
        String key = "post:" + postId;
        String lockKey = "lock:post:" + postId;
        String cache = redisTemplate.opsForValue().get(key);

        if(cache == null) {
            RLock lock = redissonClient.getLock(lockKey);
            try {
                boolean available = lock.tryLock(5, 10, TimeUnit.SECONDS);
                if (available) {
                    try {
                        cache = redisTemplate.opsForValue().get(key);
                        if (cache == null) {
                            log.info("캐싱 실패, api 호출: postId={}", postId);
                            cache = postClient.getPostAuthor(postId);

                            if (cache != null) {
                                redisTemplate.opsForValue().set(key, cache, Duration.ofMinutes(10));
                            }
                        }
                    } finally {
                        lock.unlock();
                    }
                } else {
                    log.warn("락 획득 실패");
                    return;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Lock Interrupted", e);
            }
        }
        CommentEvent mqEvent = CommentEvent.builder()
                .postId(postId)
                .commentId(event.getComment().getId())
                .writer(event.getComment().getAuthor())
                .targetUser(cache)
                .type(event.getParentId() == null ? CommentType.COMMENT : CommentType.REPLY)
                .build();

        commentEventProducer.send(mqEvent);
    }

    @Recover
    public void recover(RetryableException e, CommentSavedEvent event) {
        log.error("3번 재시도 실패: post-service 네트워크 연결 오류. event: {}", event);
    }
}
