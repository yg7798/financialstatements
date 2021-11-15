package com.tekion.accounting.fs.service.helper.cache.redis;

import com.tekion.cachesupport.lib.cache.RedisCacheFactory;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.UserContextProvider;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.tekion.accounting.fs.common.TConstants.ACCOUNTING_SMALL_CASE;


@Component
@AllArgsConstructor
public class AccountingRedisServiceImpl implements AccountingRedisService {

    private final RedisCacheFactory cacheFactory;

    @Override
    public void set(String key, Object value, int expireTime, TimeUnit timeUnit) {
        getRedisTemplate().opsForValue().set(key, value, expireTime, timeUnit);
    }

    @Override
    public Object get(String key) {
        return getRedisTemplate().opsForValue().get(key);
    }

    @Override
    public void set(String key, Object value, int expireTime, TimeUnit timeUnit, RedisSerializer<?> serializer) {
        getRedisTemplateWithSerializer(serializer).opsForValue().set(key, value, expireTime, timeUnit);
    }

    @Override
    public Object get(String key, RedisSerializer<?> serializer) {
        return getRedisTemplateWithSerializer(serializer).opsForValue().get(key);
    }

    @Override
    public List<Object> multiGet(Collection<String> keys) {
        return getRedisTemplate().opsForValue().multiGet(keys);
    }

    @Override
    public void multiDelete(Collection<String> keys) {
        getRedisTemplate().delete(keys);
    }

    @Override
    public void multiSet(Map<String,Object> keyToValueMap) {
        getRedisTemplate().opsForValue().multiSet(keyToValueMap);
    }

    @Override
    public void delete(String key) {
        getRedisTemplate().delete(key);
    }

    @Override
    public void deleteAllForCurrentDealer() {
        String pattern = getDealerLevelPattern();
        Set<String> keys = getRedisTemplate().keys(pattern);
        if (!TCollectionUtils.isEmpty(keys)) {
            getRedisTemplate().delete(keys);
        }
    }

    private RedisTemplate<String, Object> getRedisTemplate() {
        RedisTemplate<String, Object> redisTemplate = cacheFactory.getRedisTemplateForCurrentTenant();
        redisTemplate.setValueSerializer(RedisSerializer.json());
        return redisTemplate;
    }

    private RedisTemplate<String, Object> getRedisTemplateWithSerializer(RedisSerializer<?> serializer){
        RedisTemplate<String, Object> redisTemplate = cacheFactory.getRedisTemplateForCurrentTenant();
        redisTemplate.setValueSerializer(serializer);
        return redisTemplate;
    }

    private String getDealerLevelPattern(){
        return ACCOUNTING_SMALL_CASE + "_" + UserContextProvider.getCurrentTenantId() + "_" + UserContextProvider.getCurrentDealerId() + "*";
    }
}
