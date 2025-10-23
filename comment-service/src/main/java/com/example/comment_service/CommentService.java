package com.example.comment_service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import static java.util.function.Predicate.not;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;

    @Transactional
    public CommentResponse create(Long postId, CommentRequest request) {
        Comment parent = findParent(request);
        Comment comment = Comment.builder()
                .postId(postId)
                .content(request.getContent())
                .parentId(parent == null ? null : parent.getId())
                .build();

        Comment saved = commentRepository.save(comment);


        if (parent == null) {
            comment = commentRepository.save(
                    Comment.builder()
                            .id(saved.getId())
                            .postId(saved.getPostId())
                            .content(saved.getContent())
                            .deleted(saved.getDeleted())
                            .parentId(saved.getId())
                            .build()
            );
        }
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
    public void delete(Long commentId) {
        commentRepository.findById(commentId)
                .filter(comment -> !comment.getDeleted())
                .ifPresent(comment ->
                {
                    if(hasChildren(comment)) {
                        comment.delete();
                    }
                    else {
                        delete(comment);
                    }
                });
    }





    private void delete(Comment comment) {
        commentRepository.delete(comment);
        if(!comment.isRoot()) {
            commentRepository.findById(comment.getParentId())
                    .filter(Comment::getDeleted)
                    .filter(not(this::hasChildren))
                    .ifPresent(this::delete);
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
                .orElseThrow();
    }


}
