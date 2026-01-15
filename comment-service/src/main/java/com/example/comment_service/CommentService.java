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
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CommentResponse create(Long postId, CommentRequest request, String username) {
        Comment parent = findParent(request);

        Comment comment = commentRepository.save(Comment.builder()
                .postId(postId)
                .author(username)
                .content(request.getContent())
                .parentId(parent == null ? null : parent.getId())
                .build());

        comment.markSelfAsParent();

        eventPublisher.publishEvent(new CommentSavedEvent(
                comment,
                parent == null ? null : parent.getId(),
                parent == null ? null : parent.getAuthor()
        ));
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

}
