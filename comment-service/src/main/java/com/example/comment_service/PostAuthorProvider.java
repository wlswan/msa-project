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
public class PostAuthorProvider {

    private final PostClient postClient;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedissonClient redissonClient;

    @CircuitBreaker(name = "postService", fallbackMethod = "fallbackAuthor")
    @Retryable(
            retryFor = { RuntimeException.class, RetryableException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public String getAuthorWithCache(Long postId) {
        String key = "post:" + postId;
        String lockKey = "lock:post:" + postId;

        String cache = redisTemplate.opsForValue().get(key);
        if (cache != null) return cache;

        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                try {
                    cache = redisTemplate.opsForValue().get(key);
                    if (cache == null) {
                        log.info("캐시 미스, API 호출: postId={}", postId);
                        cache = postClient.getPostAuthor(postId);
                        if (cache != null) {
                            redisTemplate.opsForValue().set(key, cache, Duration.ofMinutes(10));
                        }
                    }
                    return cache;
                }
                finally {
                    lock.unlock();
                }
            }
            else {
                throw new RuntimeException("락 획득 실패 (Timeout)");
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Lock Interrupted", e);
        }
    }
    public String fallbackAuthor(Long postId, Throwable t) {
        log.error("작성자 조회 최종 실패(서킷 오픈 또는 리트라이 초과). postId: {}, 사유: {}", postId, t.getMessage());
        return null;
    }
}
