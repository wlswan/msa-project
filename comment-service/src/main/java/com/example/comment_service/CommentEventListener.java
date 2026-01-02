package com.example.comment_service;

import com.example.comment_service.rabbitmq.CommentEventProducer;
import com.example.comment_service.rabbitmq.PostClient;
import com.example.common.CommentEvent;
import com.example.common.CommentType;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentEventListener {

    private final PostClient postClient;
    private final CommentEventProducer commentEventProducer;
    private final RedisTemplate<String, String> redisTemplate;

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
        String cache = redisTemplate.opsForValue().get(key);

        if(cache == null) {
            cache = postClient.getPostAuthor(postId);

            if(cache != null) {
                redisTemplate.opsForValue().set(key,cache,Duration.ofMinutes(10));
            }
        }

        if (cache == null) {
            log.info("게시글(ID:{}) 작성자를 찾을 수 없어 알림을 보내지 않습니다.", postId);
            return;
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
