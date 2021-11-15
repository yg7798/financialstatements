package com.tekion.accounting.fs.service.helper.cache.utils;

import com.tekion.accounting.fs.service.helper.cache.dtos.OptionMinimal;
import com.tekion.accounting.fs.common.enums.CustomFieldType;
import lombok.experimental.UtilityClass;

import java.util.Map;

@UtilityClass
public class CustomFieldUtils {

	public static String getOptionDisplayLabelWithoutCode(CustomFieldType key, String id, String placeHolder, Map<CustomFieldType, Map<String, OptionMinimal>> keyToIdToOptionMap){
		if (keyToIdToOptionMap.containsKey(key)) {
			if (keyToIdToOptionMap.get(key).containsKey(id)) {
				return keyToIdToOptionMap.get(key).get(id).getName();
			}
		}
		return placeHolder;
	}

}
