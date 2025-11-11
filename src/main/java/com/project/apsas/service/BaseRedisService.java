package com.project.apsas.service;


import java.util.List;
import java.util.Map;
import java.util.Set;

public interface BaseRedisService {
    void set(String key, String value);
    void setTimeToLive(String key, long timeInSeconds);
    // cai time expire cho Day hay la time cung duoc tuy ae quyet dinh


    void hashSet(String key, String hashKey, String value);

    boolean hashExists(String key, String hashKey);

    Object Get(String key);

    public Map<String, Object> getField(String key);

    Object hashGet(String key, String field);

    List<Object> hashGetByFieldPrefix(String key, String fieldPrefix);

    Set<String> getFieldPrefix(String key);

    void delete(String key);
    void delete(String key, String field);
    void delete(String key, List<String> fields);
}
