package com.tekion.accounting.fs.common.cache.redis.helper;

import com.tekion.accounting.fs.common.cache.redis.AccountingRedisService;
import com.tekion.accounting.fs.common.cache.redis.enums.RedisCacheIdentifier;
import com.tekion.accounting.fs.common.cache.redis.helper.models.IRedisCache;
import com.tekion.core.exceptions.TBaseRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

@Slf4j
public abstract class AbstractRedisCache<Value> extends AbstractBaseRedisCache<RedisCacheIdentifier,Value> implements IRedisCache<RedisCacheIdentifier,Value> {

    @Autowired
    protected AccountingRedisService redisService;

    @Override
    protected Value doGetFromCache(RedisCacheIdentifier redisCacheIdentifier){
        return (Value) redisService.get(doGenerateKey(redisCacheIdentifier));
    }
    protected abstract RedisCacheIdentifier getCacheIdentifier();
    public Value getFromCache(){
        return getFromCache(getSafeCacheIdentifier());
    }

    protected RedisCacheIdentifier getSafeCacheIdentifier() {
        if(Objects.isNull(getCacheIdentifier())){
            throw new TBaseRuntimeException();
        }
        return getCacheIdentifier();
    }

    @Override
    protected void doSetInCache(RedisCacheIdentifier redisCacheIdentifier, Value value){
        redisService.set(redisCacheIdentifier.doGenerateKey(), value,
                (getCustomExpireTime() <= 0) ? getDefaultExpireTime() : getCustomExpireTime(),
                Objects.isNull(getCustomTimeUnit()) ? getDefaultTimeUnit() : getCustomTimeUnit());
    }

    protected abstract Value populateFromDB();

    @Override
    public Value getFromCache(RedisCacheIdentifier redisCacheIdentifier) {
        Value value = fetchFromCache(redisCacheIdentifier);
        if(Objects.isNull(value)){
            value = loadCache(redisCacheIdentifier);
        }
        return value;
    }

    @Override
    public Value loadCache(RedisCacheIdentifier redisCacheIdentifier) {
        Value value = populateFromDB();
        if (Objects.isNull(value)) {
            log.error("Cannot save Null value in redis");
            return null;
        }
        loadInCache(redisCacheIdentifier, value);
        return value;
    }

    protected Value fetchFromCache(RedisCacheIdentifier redisCacheIdentifier){
        Value value = null;
        try{
            value = doGetFromCache(redisCacheIdentifier);
        }catch (Exception exception){
            log.error("Request to get from redis cache failed with exception",exception);
            value = populateFromDB();
        }
        return value;
    }

    protected void loadInCache(RedisCacheIdentifier redisCacheIdentifier,Value value){
        try{
            doSetInCache(redisCacheIdentifier,value);
        }catch (Exception exception){
            // do nothing
            log.error("Request to load In redis cache failed with exception",exception);
        }
    }

    private String doGenerateKey(RedisCacheIdentifier redisCacheIdentifier){
        return redisCacheIdentifier.doGenerateKey();
    }

    public void invalidateCache(){
        log.error("unsupported opeartion");
        throw new TBaseRuntimeException();
    }
}
