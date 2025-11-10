package com.example.comment_service.rabbitmq;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "post-service", url = "localhost:8083")
public interface PostClient {
    @GetMapping("/posts/{postId}/author")
    String getPostAuthor(@PathVariable("postId") Long postId);
}
