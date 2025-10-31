package com.example.board_service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post,Long> {
//    @Query(value = """
//        SELECT *
//        FROM post
//        ORDER BY created_at DESC
//        LIMIT :size OFFSET :offset
//    """, nativeQuery = true)
//    List<Post> findPosts(@Param("offset") int offset, @Param("size") int size);

    @Query(value = """
        SELECT p.*
        FROM (
            SELECT id
            FROM post
            ORDER BY created_at DESC
            LIMIT :size OFFSET :offset
        ) t
        JOIN post p ON p.id = t.id
        ORDER BY p.created_at DESC
    """, nativeQuery = true)
    List<Post> findPostsJoin(@Param("offset") int offset, @Param("size") int size);

}
