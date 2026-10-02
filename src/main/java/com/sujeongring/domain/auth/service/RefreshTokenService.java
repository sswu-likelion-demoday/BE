package com.sujeongring.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String KEY_PREFIX = "refreshToken:";

    private final StringRedisTemplate redisTemplate;

    /**
     * Refresh Token 저장
     */
    public void save(
            Long userId,
            String refreshToken,
            long expirationMillis
    ) {
        String key = KEY_PREFIX + userId;

        redisTemplate.opsForValue().set(
                key,
                refreshToken,
                Duration.ofMillis(expirationMillis)
        );
    }

    /**
     * Refresh Token 조회
     */
    public String get(Long userId) {
        String key = KEY_PREFIX + userId;

        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Refresh Token 삭제
     */
    public void delete(Long userId) {
        String key = KEY_PREFIX + userId;

        redisTemplate.delete(key);
    }
}