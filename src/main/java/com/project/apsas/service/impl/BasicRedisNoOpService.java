package com.project.apsas.service.impl;

import java.time.Duration;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.project.apsas.service.BasicRedisService;

@Service
@Profile("no-db")
public class BasicRedisNoOpService implements BasicRedisService {
    @Override
    public void set(String key, Object value, Duration ttl) { /* no-op */ }

    @Override
    public <T> T get(String key, Class<T> type) { return null; }

    @Override
    public void delete(String key) { /* no-op */ }
}
