package com.project.apsas.configuration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                // Cấu hình TTL (Time-To-Live) mặc định là 10 phút
                .entryTtl(Duration.ofMinutes(10))
                // Không cache giá trị null
                .disableCachingNullValues()
                // Cấu hình Serializer cho value là JSON (thay vì Java default)
                // Điều này giúp bạn đọc được cache trên Redis-cli
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer()
                ));
    }
}
