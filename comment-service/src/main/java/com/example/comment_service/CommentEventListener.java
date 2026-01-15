package com.example.comment_service;

import com.example.comment_service.rabbitmq.CommentEventProducer;
import com.example.common.CommentEvent;
import com.example.common.CommentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentEventListener {

    private final PostAuthorProvider postAuthorProvider;
    private final CommentEventProducer commentEventProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleCommentSaved(CommentSavedEvent event) {
        Comment comment = event.getComment();
        String writer = comment.getAuthor();
        boolean isReply = event.getParentId() != null;

        // 알림 대상 결정: 답글이면 부모 댓글 작성자, 일반 댓글이면 게시글 작성자
        String targetUser = isReply
                ? event.getParentAuthor()
                : postAuthorProvider.getAuthorWithCache(comment.getPostId());

        // 알림 대상이 없거나 자기 자신이면 알림 생략
        if (targetUser == null) {
            log.warn("알림 대상을 찾을 수 없음. postId={}, commentId={}", comment.getPostId(), comment.getId());
            return;
        }
        if (targetUser.equals(writer)) {
            return;
        }

        CommentEvent mqEvent = CommentEvent.builder()
                .postId(comment.getPostId())
                .commentId(comment.getId())
                .writer(writer)
                .content(comment.getContent())
                .targetUser(targetUser)
                .type(isReply ? CommentType.REPLY : CommentType.COMMENT)
                .build();

        commentEventProducer.send(mqEvent);
    }
}
