package com.tekion.accounting.fs.common.excelGeneration.utils;

import com.tekion.accounting.fs.common.core.minimisedResource.MinimizedResourceMetaData;
import lombok.experimental.UtilityClass;

import java.util.Set;

@UtilityClass
public class MinimizedResourceUtil {

    public MinimizedResourceMetaData getMinimizedResourceMetaData(Set<String> fieldList) {
        MinimizedResourceMetaData resourceMetaData = new MinimizedResourceMetaData();
        resourceMetaData.setAddMinimizedFlag(false);
        resourceMetaData.setIncludeType(MinimizedResourceMetaData.IncludeType.INCLUSION);
        resourceMetaData.setFields(fieldList);
        return resourceMetaData;
    }
}
