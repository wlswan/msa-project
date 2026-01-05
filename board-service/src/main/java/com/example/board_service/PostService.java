package com.example.board_service;

import com.example.board_service.response.PostPageResponse;
import com.example.board_service.request.PostRequest;
import com.example.board_service.response.PostResponse;
import com.example.board_service.exception.PostNotFoundException;
import com.example.board_service.exception.UnAuthorizedRequestException;
import com.example.board_service.outbox.OutBoxRepository;
import com.example.board_service.outbox.Outbox;
import com.example.common.PostCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final OutBoxRepository outBoxRepository;

    @Transactional
    public Post createPost(PostRequest request, String username) {
        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .author(username)
                .build();

        Post savedPost = postRepository.save(post);

        PostCreatedEvent event = PostCreatedEvent.from(post);
        try {
            String payload = objectMapper.writeValueAsString(event);

            Outbox outbox = Outbox.builder()
                    .payload(payload)
                    .eventType("POST_CREATED")
                    .aggregateId(savedPost.getId())
                    .build();

            outBoxRepository.save(outbox);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return savedPost;
    }

    public Post getPost(Long postId) {
        return postRepository.findById(postId).orElseThrow(PostNotFoundException::new);
    }

    public List<Post>  getAllPosts(){
        return postRepository.findAll();
    }

    public Post updatePost(Long id, PostRequest request, String username, String role) {
            Post post = getPost(id);

            if (!post.getAuthor().equals(username) && !role.equals("ROLE_ADMIN")) {
                throw new UnAuthorizedRequestException();
            }

            post.update(request.getTitle(), request.getContent());
            return postRepository.save(post);
    }

    public void deletePost(Long id, String username, String role) {
        Post post = getPost(id);

        if (!post.getAuthor().equals(username) && !role.equals("ROLE_ADMIN")) {
            throw new UnAuthorizedRequestException();
        }

        postRepository.delete(post);
    }

    public PostPageResponse getPosts(int page, int limit) {
        int offset = (page-1) * limit;
        List<PostResponse> postResponses = postRepository.findPostsJoin(offset, limit).stream().map(PostResponse::from).toList();
        long totalCount = postRepository.count();

        return new PostPageResponse(postResponses, page, limit, totalCount);

    }

    public String getPostAuthorUsername(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);
        return post.getAuthor();
    }

}

