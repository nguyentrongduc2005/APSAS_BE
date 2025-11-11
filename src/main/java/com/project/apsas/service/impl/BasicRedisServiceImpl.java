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
        // Lưu key-value đơn giản. VD: set("user:1", "lin_nguyen")
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void setTimeToLive(String key, long timeInSeconds) {
        // Đặt TTL cho key. VD: setTimeToLive("session:abc", 3600) - hết hạn sau 1h
        redisTemplate.expire(key, timeInSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void hashSet(String key, String hashKey, String value) {
        // Lưu field vào hash. VD: hashSet("user:1", "email", "test@gmail.com")
        hashOperations.put(key, hashKey, value);
    }

    @Override
    public boolean hashExists(String key, String hashKey) {
        //  Kiểm tra field có tồn tại. VD: hashExists("user:1", "email") → true/false
        return hashOperations.hasKey(key, hashKey);
    }

    @Override
    public Object Get(String key) {
        // Lấy giá trị key đơn. VD: Get("user:1") → "an_nguyen"
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public Map<String, Object> getField(String key) {
        // Lấy tất cả field trong hash. VD: getField("user:1") → {email: "test@gmail.com", name: "An"}
        return hashOperations.entries(key);
    }

    @Override
    public Object hashGet(String key, String field) {
        // Lấy 1 field cụ thể. VD: hashGet("user:1", "email") → "test@gmail.com"
        return hashOperations.get(key, field);
    }

    @Override
    public List<Object> hashGetByFieldPrefix(String key, String fieldPrefix) {
        //  Lấy values của các field bắt đầu bằng prefix. VD: hashGetByFieldPrefix("user:1", "addr_") → ["123 Street", "456 Road"]
        List<Object> list = new ArrayList<>();
        Map<String, Object> entries = hashOperations.entries(key);

        for (Map.Entry<String, Object> entry : entries.entrySet()) {
            if (entry.getKey().startsWith(fieldPrefix)) {
                list.add(entry.getValue());
            }
        }
        return list;
    }

    @Override
    public Set<String> getFieldPrefix(String key) {
        // Lấy tất cả tên field. VD: getFieldPrefix("user:1") → ["email", "name", "age"]
        return hashOperations.entries(key).keySet();
    }

    @Override
    public void delete(String key) {
        // Xóa toàn bộ key. VD: delete("user:1")
        redisTemplate.delete(key);
    }

    @Override
    public void delete(String key, String field) {
        // Xóa 1 field khỏi hash. VD: delete("user:1", "email")
        hashOperations.delete(key, field);
    }

    @Override
    public void delete(String key, List<String> fields) {
        // Xóa nhiều field. VD: delete("user:1", List.of("email", "phone"))
        for (String field : fields) {
            hashOperations.delete(key, field);
        }
    }
}