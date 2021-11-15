package com.tekion.accounting.fs.service.helper.cache.redis;

import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface AccountingRedisService {

    void set(String key, Object value, int expireTime, TimeUnit timeUnit);

    Object get(String key);

    void set(String key, Object value, int expireTime, TimeUnit timeUnit, RedisSerializer<?> serializer);

    Object get(String key, RedisSerializer<?> serializer);

    List<Object> multiGet(Collection<String> keys);

    void multiDelete(Collection<String> keys);

    void multiSet(Map<String,Object> keyToValueMap);

    void delete(String key);

    void deleteAllForCurrentDealer();

}
