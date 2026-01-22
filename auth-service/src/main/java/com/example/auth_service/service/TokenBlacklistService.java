package com.example.auth_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:";

    public void addToBlacklist(String token, long expirationTimeInMillis) {
        String key = BLACKLIST_PREFIX + token;
        long ttlInSeconds = expirationTimeInMillis / 1000;

        if (ttlInSeconds > 0) {
            redisTemplate.opsForValue().set(key, "logout", ttlInSeconds, TimeUnit.SECONDS);
        }
    }

    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
