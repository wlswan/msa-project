package com.example.comment_service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<?> create(@PathVariable Long postId,
                                    @RequestBody CommentRequest request,
                                    @RequestHeader("X-User-Name") String username) {
        CommentResponse response = commentService.create(postId, request ,username);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> delete(@PathVariable Long commentId,
                                    @RequestHeader("X-User-Name") String username,
                                    @RequestHeader("X-User-Role") String role) {
        commentService.delete(commentId,username,role);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public CommentPageResponse getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return commentService.getCommentsByPost(postId, page, size);
    }
}
