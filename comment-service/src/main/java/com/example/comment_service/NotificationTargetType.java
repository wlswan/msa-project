package com.example.comment_service;

public enum NotificationTargetType {
    POST_AUTHOR,      // 게시글 작성자에게 알림 (일반 댓글)
    PARENT_AUTHOR     // 부모 댓글 작성자에게 알림 (대댓글)
}
