package com.project.apsas.service.impl;


import com.project.apsas.service.BaseRedisService;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class BasicRedisServiceImpl implements BaseRedisService {
    private final RedisTemplate<String,Object> redisTemplate;
    private final HashOperations<String,String,Object> hashOperations;

    public BasicRedisServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOperations = redisTemplate.opsForHash();
    }

    @Override
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void setTimeToLive(String key, long timeInSeconds) {
        redisTemplate.expire(key, timeInSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void hashSet(String key, String hashKey, String value) {
        hashOperations.put(key,hashKey,value);
    }

    @Override
    public boolean hashExists(String key, String hashKey) {
        return hashOperations.hasKey(key, hashKey);
    }

    @Override
    public Object Get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public Map<String, Object> getField(String key) {
        return hashOperations.entries(key);
    }

    @Override
    public Object hashGet(String key, String field) {
        return hashOperations.get(key, field);
    }

    @Override
    public List<Object> hashGetByFieldPrefix(String key, String fieldPrefix) {
        List<Object> list = new ArrayList<>();
        Map<String,Object> map = hashOperations.entries(key);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            // Kiểm tra xem field (key con) có bắt đầu bằng prefix không
            if (entry.getKey().startsWith(fieldPrefix)) {
                list.add(entry.getValue());
            }
        }
        return list;
    }

    @Override
    public  Set<String> getFieldPrefix(String key) {
        return hashOperations.entries(key).keySet();
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public void delete(String key, String field) {
        hashOperations.delete(key,field);
    }

    @Override
    public void delete(String key, List<String> fields) {
        for(String field:fields){
            hashOperations.delete(key,field);
        }
    }
}
