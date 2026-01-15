package com.example.comment_service;

import com.example.comment_service.rabbitmq.PostClient;
import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostAuthorProvider {

    private final PostClient postClient;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedissonClient redissonClient;

    private static final long LOCK_WAIT_TIME = 5;
    private static final long LOCK_LEASE_TIME = 10;
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    @CircuitBreaker(name = "postService", fallbackMethod = "fallbackAuthor")
    @Retryable(retryFor = {RetryableException.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public String getAuthorWithCache(Long postId) {
        String key = "post:" + postId;

        String cached = getFromCache(key);
        if (cached != null) {
            return cached;
        }

        return executeWithLock(
                "lock:" + key,
                () -> fetchAndCache(postId, key),
                () -> fetchFromApi(postId)
        );
    }

    private String fetchAndCache(Long postId, String key) {
        String cached = getFromCache(key);
        if (cached != null) {
            return cached;
        }

        log.info("캐시 미스, API 호출: postId={}", postId);
        String author = postClient.getPostAuthor(postId);
        saveToCache(key, author);
        return author;
    }

    private String fetchFromApi(Long postId) {
        log.warn("Redis 사용 불가, API 직접 호출: postId={}", postId);
        return postClient.getPostAuthor(postId);
    }

    private String executeWithLock(String lockKey, Supplier<String> action, Supplier<String> fallback) {
        RLock lock = null;
        boolean locked = false;

        try {
            lock = redissonClient.getLock(lockKey);
            locked = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);

            if (locked) {
                return action.get();
            }
            log.warn("락 획득 실패 (대기 시간 초과)");
            return fallback.get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("락 대기 중 인터럽트 발생");
            return fallback.get();

        } catch (RedisException e) {
            // Redisson 예외: RedisConnectionException, RedisTimeoutException 등 모두 포함
            log.warn("Redisson 오류 발생: {}", e.getMessage());
            return fallback.get();

        } catch (DataAccessException e) {
            // Spring Data Redis 예외: RedisConnectionFailureException 등 모두 포함
            log.warn("Spring Redis 오류 발생: {}", e.getMessage());
            return fallback.get();

        } finally {
            unlockSafely(lock, locked);
        }
    }

    private void unlockSafely(RLock lock, boolean locked) {
        if (!locked || lock == null) {
            return;
        }
        try {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        } catch (RedisException e) {
            log.warn("락 해제 실패: {}", e.getMessage());
        }
    }

    private String getFromCache(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (DataAccessException e) {
            log.warn("캐시 조회 실패: {}", e.getMessage());
            return null;
        }
    }

    private void saveToCache(String key, String value) {
        if (value == null) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(key, value, CACHE_TTL);
        } catch (DataAccessException e) {
            log.warn("캐시 저장 실패: {}", e.getMessage());
        }
    }

    public String fallbackAuthor(Long postId, Throwable t) {
        log.error("작성자 조회 최종 실패 (서킷 오픈 또는 재시도 초과). postId: {}, 사유: {}", postId, t.getMessage());
        return null;
    }
}
