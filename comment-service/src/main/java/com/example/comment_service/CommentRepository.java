package com.example.comment_service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment,Long> {
    
    @Query(value = """
    SELECT c.*
    FROM (
        SELECT id
        FROM comment
        WHERE post_id = :postId
        ORDER BY parent_id ASC, id ASC
        LIMIT :limit OFFSET :offset
    ) t
    LEFT JOIN comment c ON t.id = c.id
""", nativeQuery = true)
    List<Comment> findCommentsByPostId(
            @Param("postId") Long postId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );


    @Query(value = "SELECT COUNT(*) FROM comment WHERE post_id = :postId", nativeQuery = true)
    long countCommentsByPostId(@Param("postId") Long postId);


    @Query(value = """
        SELECT COUNT(*) 
        FROM comment 
        WHERE post_id = :postId 
          AND parent_id = :commentId
    """, nativeQuery = true)
    Long countChild(@Param("postId") Long postId, @Param("commentId") Long commentId);
}
