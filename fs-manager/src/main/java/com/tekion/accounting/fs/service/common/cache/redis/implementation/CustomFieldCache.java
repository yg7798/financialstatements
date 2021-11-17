package com.tekion.accounting.fs.service.common.cache.redis.implementation;

import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.service.common.cache.dtos.OptionDeptMinimal;
import com.tekion.accounting.fs.service.common.cache.dtos.OptionMinimal;
import com.tekion.accounting.fs.service.common.cache.redis.dto.CustomFieldCacheDto;
import com.tekion.accounting.fs.service.common.cache.redis.enums.RedisCacheIdentifier;
import com.tekion.accounting.fs.service.common.cache.redis.helper.AbstractRedisCache;
import com.tekion.accounting.fs.common.enums.AccountingCustomField;
import com.tekion.accounting.fs.common.enums.CustomFieldType;
import com.tekion.clients.preference.beans.CustomField;
import com.tekion.clients.preference.beans.Option;
import com.tekion.clients.preference.client.PreferenceClient;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.TStringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
@AllArgsConstructor
@Slf4j
public class CustomFieldCache extends AbstractRedisCache<CustomFieldCacheDto> {

    private final PreferenceClient preferenceClient;

    @Override
    protected RedisCacheIdentifier getCacheIdentifier() {
        return RedisCacheIdentifier.CUSTOM_FIELD;
    }

    @Override
    protected CustomFieldCacheDto populateFromDB() {
        CustomFieldCacheDto customFieldCacheDto = new CustomFieldCacheDto();
        try {
            customFieldCacheDto.setKeyToOptionIdToOptionMap(getKeyToOptionIdToOptionMap(preferenceClient.getCustomFields(TConstants.ACCOUNTS_CF_ASSET_TYPE).getData()));
        } catch (Exception exception) {
            log.error("Exception while fetching custom field map from preference service");
            return null;
        }
        return customFieldCacheDto;
    }

    @Override
    public void invalidateCache(){
        log.info("started invalidating cache : {}", getSafeCacheIdentifier());
        redisService.delete(getSafeCacheIdentifier().doGenerateKey());
        log.info("finished invalidating cache: {}", getSafeCacheIdentifier());
    }

    private static Map<CustomFieldType, Map<String, OptionMinimal>> getKeyToOptionIdToOptionMap(List<CustomField> customFieldList) {
        Map<CustomFieldType, Map<String, OptionMinimal>> keyToOptionMap = new HashMap<>();
        TCollectionUtils.nullSafeList(customFieldList)
                .stream()
                .filter(cf -> AccountingCustomField.keyToCustomFieldMap.containsKey(cf.getKey()))
                .forEach(customField -> {
                    if (CustomFieldType.DEPARTMENT.equals(CustomFieldType.valueOf(customField.getKey()))) {
                        Map<String, OptionMinimal> optionDeptMinimalMap = TCollectionUtils.nullSafeMap(TCollectionUtils.nullSafeList(customField.getOptions())
                                .stream()
                                .collect(Collectors.toMap(
                                        Option::getId, CustomFieldCache::getDeptMinimalOption, (oldValue, newValue) -> newValue)));
                        keyToOptionMap.put(CustomFieldType.valueOf(customField.getKey()), optionDeptMinimalMap);
                    } else {
                        Map<String, OptionMinimal> optionMinimalMap = TCollectionUtils.nullSafeMap(TCollectionUtils.nullSafeList(customField.getOptions())
                                .stream()
                                .collect(Collectors.toMap(
                                        Option::getId, CustomFieldCache::getMinimalOption, (oldValue, newValue) -> newValue)));
                        keyToOptionMap.put(CustomFieldType.valueOf(customField.getKey()), optionMinimalMap);
                    }
                });
        return keyToOptionMap;
    }

    private static OptionMinimal getMinimalOption(Option option) {
        String code = null;
        String name = null;
        if (TStringUtils.isNotBlank(option.getName())) {
            name = option.getName();
        }
        if (TCollectionUtils.isNotEmpty(option.getExtras()) && option.getExtras().containsKey(TConstants.CODE) && TCollectionUtils.isNotEmpty(option.getExtras().get(TConstants.CODE))) {
            code = option.getExtras().get(TConstants.CODE).get(0);
        }
        return OptionMinimal
                .builder()
                .code(code)
                .name(name)
                .build();
    }

    private static OptionDeptMinimal getDeptMinimalOption(Option option) {
        OptionMinimal optionMinimal = getMinimalOption(option);
        String deptType = null;
        if (TCollectionUtils.isNotEmpty(option.getExtras()) && option.getExtras().containsKey(TConstants.DEPT_TYPE_CUSTOM_FIELD_OPTION) && TCollectionUtils.isNotEmpty(option.getExtras().get(TConstants.DEPT_TYPE_CUSTOM_FIELD_OPTION))) {
            deptType = option.getExtras().get(TConstants.DEPT_TYPE_CUSTOM_FIELD_OPTION).get(0);
        }
        OptionDeptMinimal optionDeptMinimal = new OptionDeptMinimal();
        optionDeptMinimal.setCode(optionMinimal.getCode());
        optionDeptMinimal.setName(optionMinimal.getName());
        optionDeptMinimal.setDeptType(deptType);
        return optionDeptMinimal;
    }
}
