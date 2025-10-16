package com.example.board_service;

import com.example.board_service.dto.AuthValidateResponse;
import com.example.board_service.dto.PostRequest;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final AuthClient authClient;

    public Post createPost(PostRequest request, String token) {
        try {
            AuthValidateResponse response = authClient.validateToken(token);

            if(!response.isSuccess() || !response.getData().isValid()) {
                throw new RuntimeException("인증 실패: " + response.getMessage());
            }
            String username = response.getData().getUsername();

            Post post = Post.builder()
                    .title(request.getTitle())
                    .content(request.getContent())
                    .author(username)
                    .build();
            return postRepository.save(post);
        }
        catch (FeignException e) {
            throw new IllegalArgumentException("통신 실패: " + e.getMessage());

        }

    }
    public Post getPost(Long postId) {
        return postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));
    }

    public List<Post>  getAllPosts(){
        return postRepository.findAll();
    }

    public Post updatePost(Long id, PostRequest request, String token) {
        try {
            AuthValidateResponse response = authClient.validateToken(token);

            if (!response.isSuccess() || !response.getData().isValid()) {
                throw new RuntimeException("인증 실패: " + response.getMessage());
            }
            String username = response.getData().getUsername();
            String role = response.getData().getRole();
            Post post = getPost(id);

            if (!post.getAuthor().equals(username) || !role.equals("ROLE_ADMIN")) {
                throw new RuntimeException("작성자 본인 또는 관리자만 수정할 수 있습니다.");
            }

            post.update(request.getTitle(), request.getContent());

            return postRepository.save(post);
        }
        catch (FeignException e) {
            throw new IllegalArgumentException("통신 실패: " + e.getMessage());

        }
    }

    public void deletePost(Long id, String token) {
        try {
            AuthValidateResponse response = authClient.validateToken(token);

            if (!response.isSuccess() || !response.getData().isValid()) {
                throw new RuntimeException("인증 실패: " + response.getMessage());
            }
            String username = response.getData().getUsername();
            String role = response.getData().getRole();
            Post post = getPost(id);

            if (!post.getAuthor().equals(username) || !role.equals("ROLE_ADMIN")) {
                throw new RuntimeException("작성자 본인 또는 관리자만 삭제할 수 있습니다.");
            }

            postRepository.delete(post);
        }
        catch (FeignException e) {
            throw new IllegalArgumentException("통신 실패: " + e.getMessage());

        }
    }
}
