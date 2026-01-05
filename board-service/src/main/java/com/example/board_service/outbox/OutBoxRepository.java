package com.example.board_service.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutBoxRepository extends JpaRepository<Outbox,Long> {
    List<Outbox> findAllByStatusOrderByIdAsc(OutboxStatus status);
}
