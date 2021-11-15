package com.tekion.accounting.fs.common.cache.redis.helper;

import java.util.concurrent.TimeUnit;

public abstract class AbstractBaseRedisCache<Key,Value> {
    protected static final int DEFAULT_EXPIRE_TIME = 1;
    protected static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MINUTES;

    protected int getCustomExpireTime() {
        return getDefaultExpireTime();
    }

    protected TimeUnit getCustomTimeUnit(){
        return getDefaultTimeUnit();
    }

    protected abstract Value doGetFromCache(Key key);

    protected abstract void doSetInCache(Key key,Value value);

    public int getDefaultExpireTime() {
        return DEFAULT_EXPIRE_TIME;
    }

    public TimeUnit getDefaultTimeUnit() {
        return DEFAULT_TIME_UNIT;
    }
}
