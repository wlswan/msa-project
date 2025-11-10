package com.example.comment_service;

import com.example.comment_service.exception.CommentNotFoundException;
import com.example.comment_service.exception.InvalidParentException;
import com.example.comment_service.exception.UnauthorizedRequestException;
import com.example.comment_service.rabbitmq.PostClient;
import com.example.common.CommentEvent;
import com.example.comment_service.rabbitmq.CommentEventProducer;
import com.example.common.CommentType;
import com.example.comment_service.request.CommentRequest;
import com.example.comment_service.response.CommentPageResponse;
import com.example.comment_service.response.CommentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import static java.util.function.Predicate.not;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostClient postClient;
    private final RedisTemplate<String, String> redisTemplate;
    private final CommentEventProducer commentEventProducer;

    @Transactional
    public CommentResponse create(Long postId, CommentRequest request, String username) {
        Comment parent = findParent(request);
        String postAuthor = getPostAuthor(postId);

        Comment comment = commentRepository.save(Comment.builder()
                .postId(postId)
                .author(username)
                .content(request.getContent())
                .parentId(parent == null ? null : parent.getId())
                .build());

        comment.markSelfAsParent();

        CommentEvent event = CommentEvent.builder()
                .postId(postId)
                .commentId(comment.getId())
                .writer(username)
                .targetUser(parent == null ? postAuthor : parent.getAuthor())
                .content(request.getContent())
                .type(parent == null ? CommentType.COMMENT : CommentType.REPLY)
                .build();
        commentEventProducer.send(event);

        return CommentResponse.from(comment);
    }
    public CommentPageResponse getCommentsByPost(Long postId, int page, int size) {
        int offset = page * size;

        List<CommentResponse> commentResponses = commentRepository.findCommentsByPostId(postId, offset, size).stream().map(CommentResponse::from)
                .toList();

        long totalCount = commentRepository.countCommentsByPostId(postId);

        return new CommentPageResponse(commentResponses, page, size, totalCount);
    }


    @Transactional
    public void delete(Long commentId, String username, String role) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(CommentNotFoundException::new);

        if (!comment.getAuthor().equals(username) && !"ADMIN".equals(role)) {
            throw new UnauthorizedRequestException();
        }

        if (!comment.getDeleted()) {
            if (hasChildren(comment)) {
                comment.delete();
            } else {
                deleteRecursively(comment);
            }
        }
    }

    private void deleteRecursively(Comment comment) {
        commentRepository.delete(comment);
        if(!comment.isRoot()) {
            commentRepository.findById(comment.getParentId())
                    .filter(Comment::getDeleted)
                    .filter(not(this::hasChildren))
                    .ifPresent(this::deleteRecursively);
        }
    }

    private boolean hasChildren(Comment comment) {
        return commentRepository.countChild(comment.getPostId(), comment.getId()) >= 2;
    }


    private Comment findParent(CommentRequest request) {
        Long parentId = request.getParentId();
        if(parentId == null){
            return null;
        }
        return commentRepository.findById(parentId)
                .filter(comment -> !comment.getDeleted())
                .filter(Comment::isRoot)
                .orElseThrow(InvalidParentException::new);
    }

    private String getPostAuthor(Long postId) {
        String key = "post:" + postId;

        String author = redisTemplate.opsForValue().get(key);
        if (author != null) {
            log.info("캐싱 성공 postId={}, author={}", postId, author);
            return author;
        }

        log.warn("캐싱 실패 postId={} -> post-service로 조회 시도", postId);
        author = postClient.getPostAuthor(postId);

        if (author != null) {
            redisTemplate.opsForValue().set(key, author, Duration.ofHours(1));
            log.info("Redis 캐싱 완료: {} = {}", key, author);
        }

        return author;
    }

}
