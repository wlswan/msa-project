package com.example.board_service;

import com.example.board_service.dto.PostRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Logger;
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
                                    @RequestHeader("X-User-Name") String username)
    {
        Post post = postService.createPost(postRequest, username);
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
                                        @RequestHeader("X-User-Name") String username,
                                        @RequestHeader("X-User-Role") String role) {
        Post updated = postService.updatePost(id, postRequest, username, role);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id,
                                        @RequestHeader("X-User-Name") String username,
                                        @RequestHeader("X-User-Role") String role){
        postService.deletePost(id,username,role);
        return ResponseEntity.noContent().build();
    }
}
