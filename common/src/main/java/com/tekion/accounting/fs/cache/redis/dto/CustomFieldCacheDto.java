package com.tekion.accounting.fs.cache.redis.dto;


import com.tekion.accounting.fs.cache.dtos.OptionMinimal;
import com.tekion.accounting.fs.enums.CustomFieldType;
import lombok.Data;

import java.util.Map;

@Data
public class CustomFieldCacheDto {
    Map<CustomFieldType, Map<String, OptionMinimal>> KeyToOptionIdToOptionMap;
}
