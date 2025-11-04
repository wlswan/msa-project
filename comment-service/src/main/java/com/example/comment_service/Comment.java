package com.example.comment_service;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Comment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long postId;
    private String author;
    private String content;
    @Builder.Default
    private Boolean deleted = false;
    private Long parentId;

    public void update(String content) {
        this.content = content;
    }

    public boolean isRoot() {
        return parentId.longValue() == id;
    }
    public void delete()  {
        deleted = true;
    }

}
