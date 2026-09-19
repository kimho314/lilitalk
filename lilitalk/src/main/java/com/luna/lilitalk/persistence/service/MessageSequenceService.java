package com.luna.lilitalk.persistence.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageSequenceService {

    private final RedisTemplate<String, String> redisTemplate;
    private final String prefix = "chat:sequence";

    public MessageSequenceService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Long getNextSequence(Long chatRoomId) {
        final String key = "${prefix}:${chatRoomId}";

        // INCR 명령어를 사용하여 원자적인 증가
        return redisTemplate.opsForValue().increment(key) == null ? 1L
            : redisTemplate.opsForValue().increment(key);
    }
}
