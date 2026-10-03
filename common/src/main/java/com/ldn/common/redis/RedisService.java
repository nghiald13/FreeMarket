package com.ldn.common.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public void setIfPresentKeepTTL(String key, Object value) {
        redisTemplate.opsForValue().setIfPresent(key, value, Expiration.keepTtl());
    }

    public <T> T get(String key, Class<T> type) {
        Object raw = redisTemplate.opsForValue().get(key);
        if (raw == null) return null;
        return objectMapper.convertValue(raw, type);
    }

    public <T> T getAndDelete(String key, Class<T> type) {
        Object raw = this.redisTemplate.opsForValue().getAndDelete(key);
        if (raw == null) return null;
        return objectMapper.convertValue(raw, type);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public <T> Set<T> sMember(String key, Class<T> type) {
        Set<Object> objects = this.redisTemplate.opsForSet().members(key);
        return objects.stream().map(object -> objectMapper.convertValue(object, type)).collect(Collectors.toSet());
    }

    public void sSet(String key, Object value, long timeout, TimeUnit unit) {
        this.redisTemplate.opsForSet().add(key, value);
        this.redisTemplate.expire(key, Expiration.from(timeout, unit));
    }

    public void sDelete(String key, String value) {
        this.redisTemplate.opsForSet().remove(key, value);
    }

}
