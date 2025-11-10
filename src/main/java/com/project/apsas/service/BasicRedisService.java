package com.project.apsas.service;

import java.time.Duration;

public interface BasicRedisService {
    void set(String key, Object value, Duration ttl);
    <T> T get(String key, Class<T> type);
    void delete(String key);
}
