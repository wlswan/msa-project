package com.example.board_service;

import com.example.board_service.dto.PostRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody PostRequest postRequest,
                                    @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        Post post = postService.createPost(postRequest, token);
        return ResponseEntity.ok(post);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPost(@PathVariable Long id) {
        Post post = postService.getPost(id);
        return ResponseEntity.ok(post);
    }

    @GetMapping
    public ResponseEntity<?> getAllPosts() {
        List<Post> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(@PathVariable Long id,
                                        @RequestBody PostRequest postRequest,
                                        @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {

        Post updated = postService.updatePost(id, postRequest, token);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id,
                                        @RequestHeader(HttpHeaders.AUTHORIZATION) String token){
        postService.deletePost(id,token);
        return ResponseEntity.noContent().build();
    }
}
