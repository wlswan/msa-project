//package com.example.comment_service.redis;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class RedisLikeService {
//    private final RedisTemplate<String,String> redisTemplate;
//
//    private String getLikeKey(Long commentId) {
//        return "like:comment:" + commentId;
//    }
//
//    public void like(Long commentId, String username) {
//        redisTemplate.opsForSet().add(getLikeKey(commentId),username);
//    }
//
//    public void unlike(Long commentId, String username) {
//        redisTemplate.opsForSet().remove(getLikeKey(commentId), username);
//    }
//
//    public boolean isLiked(Long commentId,String username) {
//        Boolean member = redisTemplate.opsForSet().isMember(getLikeKey(commentId), username);
//        return Boolean.TRUE.equals(member);
//        //레디스는 객체 타입을 반환해서 null 상태를 가질수 있음
//    }
//
//    public Long getLikeCount(Long commentId) {
//        return redisTemplate.opsForSet().size(getLikeKey(commentId));
//    }
//
//
//}
