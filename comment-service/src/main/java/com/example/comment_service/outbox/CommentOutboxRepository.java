package com.example.comment_service.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentOutboxRepository extends JpaRepository<CommentOutbox, Long> {
    List<CommentOutbox> findAllByStatusOrderByIdAsc(OutboxStatus status);
}