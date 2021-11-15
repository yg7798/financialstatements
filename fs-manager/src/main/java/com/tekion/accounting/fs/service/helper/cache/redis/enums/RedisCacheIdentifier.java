package com.tekion.accounting.fs.service.helper.cache.redis.enums;


import com.tekion.accounting.fs.common.TConstants;
import com.tekion.core.utils.UserContextProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.tekion.accounting.fs.service.helper.cache.redis.enums.CacheLevel.DEALER_LEVEL;


/** this is can be used for static key. Key format will be cacheLevel pattern + redis cache identifier name
 */
@Getter
@AllArgsConstructor
public enum RedisCacheIdentifier {
    CUSTOM_FIELD(DEALER_LEVEL),
    KEYWORD_CONFIG(DEALER_LEVEL);

    private final CacheLevel cacheLevel;

    public String doGenerateKey(){
        return getCacheLevelPattern(cacheLevel) + "_" + this.name();
    }

    private String getCacheLevelPattern(CacheLevel cacheLevel){
        String keyPrefix = null;
        switch (cacheLevel) {
            case DEALER_LEVEL:
                keyPrefix = TConstants.ACCOUNTING_SMALL_CASE + "_" + UserContextProvider.getCurrentTenantId()+"_"+UserContextProvider.getCurrentDealerId();
                break;
            case TENANT_LEVEL:
                keyPrefix = TConstants.ACCOUNTING_SMALL_CASE + "_" + UserContextProvider.getCurrentTenantId()+"_"+ TConstants.ZERO_STRING;
                break;
            case GLOBAL_LEVEL:
                keyPrefix = TConstants.ACCOUNTING_SMALL_CASE + "_" + TConstants.ZERO_STRING + "_" + TConstants.ZERO_STRING;
                break;
        }
        return keyPrefix;
    }
}
