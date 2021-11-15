package com.tekion.accounting.fs.service.helper.cache.redis.helper.models;

public interface IRedisCache<Key,Value> {
    Value getFromCache(Key key);

    Value loadCache(Key key);
}
