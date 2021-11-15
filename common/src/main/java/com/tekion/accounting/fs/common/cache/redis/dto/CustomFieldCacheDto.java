package com.tekion.accounting.fs.common.cache.redis.dto;


import com.tekion.accounting.fs.common.cache.dtos.OptionMinimal;
import com.tekion.accounting.fs.common.enums.CustomFieldType;
import lombok.Data;

import java.util.Map;

@Data
public class CustomFieldCacheDto {
    Map<CustomFieldType, Map<String, OptionMinimal>> KeyToOptionIdToOptionMap;
}
