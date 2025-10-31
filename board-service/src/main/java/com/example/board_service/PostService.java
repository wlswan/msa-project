package com.example.board_service;

import com.example.board_service.dto.PostPageResponse;
import com.example.board_service.dto.PostRequest;
import com.example.board_service.dto.PostResponse;
import com.example.board_service.exception.PostNotFoundException;
import com.example.board_service.exception.UnAuthorizedRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    public Post createPost(PostRequest request, String username) {
        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .author(username)
                .build();
        return postRepository.save(post);
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


}

