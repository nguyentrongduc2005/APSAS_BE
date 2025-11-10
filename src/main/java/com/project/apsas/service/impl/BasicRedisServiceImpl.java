package com.project.apsas.service.impl;

import java.time.Duration;

import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.project.apsas.service.BasicRedisService;

import lombok.RequiredArgsConstructor;

@Service
@Profile("db")
@RequiredArgsConstructor
public class BasicRedisServiceImpl implements BasicRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void set(String key, Object value, Duration ttl) {
        if (ttl == null) {
            redisTemplate.opsForValue().set(key, value);
        } else {
            redisTemplate.opsForValue().set(key, value, ttl);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String key, Class<T> type) {
        Object v = redisTemplate.opsForValue().get(key);
        return v == null ? null : (T) v;
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
