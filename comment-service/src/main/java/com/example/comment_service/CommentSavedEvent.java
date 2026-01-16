package com.example.comment_service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CommentSavedEvent {
    private final Comment comment;
    private final NotificationTargetType targetType;
    private final String targetUser;
}
