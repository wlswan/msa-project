package com.example.board_service;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
public class Post {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String content;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String author;

    @PrePersist
    public void onCrete(){
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }


    public Post update(String title, String content) {
        this.title = title;
        this.content = content;
        return this;
    }
}
