package com.tekion.accounting.fs.cache;

import com.tekion.accounting.fs.TConstants;
import com.tekion.accounting.fs.cache.dtos.OptionMinimal;
import com.tekion.accounting.fs.cache.redis.dto.CustomFieldCacheDto;
import com.tekion.accounting.fs.cache.redis.enums.RedisCacheIdentifier;
import com.tekion.accounting.fs.cache.redis.implementation.CustomFieldCache;
import com.tekion.accounting.fs.enums.AccountingCustomField;
import com.tekion.accounting.fs.enums.CustomFieldType;
import com.tekion.clients.preference.beans.CustomField;
import com.tekion.clients.preference.beans.Option;
import com.tekion.clients.preference.client.PreferenceClient;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.TStringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Sets;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.tekion.accounting.fs.TConstants.ACCOUNTS_CF_ASSET_TYPE;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomFieldConfig {

    private final CustomFieldCache customFieldCache;

    public Map<CustomFieldType, Map<String, OptionMinimal>> getKeyToIdToOptionMap() {
        CustomFieldCacheDto customFieldCacheDto = customFieldCache.getFromCache(RedisCacheIdentifier.CUSTOM_FIELD);
        if (Objects.isNull(customFieldCacheDto)) {
            return new HashMap<>();
        }
        return TCollectionUtils.nullSafeMap(customFieldCacheDto.getKeyToOptionIdToOptionMap());
    }

    public static Map<CustomFieldType, Map<String, OptionMinimal>> getKeyToOptionIdToOptionMap(PreferenceClient preferenceClient) {
        try {
            return getKeyToOptionIdToOptionMap(preferenceClient.getCustomFields(ACCOUNTS_CF_ASSET_TYPE).getData());
        }
        catch (Exception e){
            log.error("Custom field CacheLoading failed : External Call error while caching customField config  ",e);
            return null;
        }
    }

    private static Map<CustomFieldType, Map<String, OptionMinimal>> getKeyToOptionIdToOptionMap(List<CustomField> customFieldList) {

        return TCollectionUtils.nullSafeList(customFieldList)
                .stream()
                .filter(cf -> AccountingCustomField.keyToCustomFieldMap.containsKey(cf.getKey()))
                .collect(Collectors.toMap
                        (cf -> CustomFieldType.valueOf(cf.getKey()),
                                cf -> TCollectionUtils.nullSafeList(cf.getOptions()).stream().collect(Collectors.toMap(
                                        Option::getId, option -> getMinimalOption(option),(oldValue, newValue) -> newValue
                                )))
                );
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

    private static final Set<String> customFieldTypeList = Sets.newHashSet(
            Arrays.asList(
                    CustomFieldType.DEPARTMENT.name(),
                    CustomFieldType.FINANCIAL_STATEMENT_GROUP.name(),
                    CustomFieldType.FINANCIAL_STATEMENT_SUB_GROUP.name(),
                    CustomFieldType.ACCOUNT_SUBTYPE.name())
    );
}
