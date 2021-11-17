package com.tekion.accounting.fs.service.common.cache.redis.implementation;


import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.service.common.cache.redis.helper.AbstractRedisCache;
import com.tekion.accounting.fs.service.common.cache.redis.enums.RedisCacheIdentifier;
import com.tekion.clients.preference.beans.DisplayLabel;
import com.tekion.clients.preference.client.PreferenceClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;


@Component
@Slf4j
@Data
public class KeywordConfigCache extends AbstractRedisCache<DisplayLabel> {
    private final PreferenceClient preferenceClient;

    @Override
    protected RedisCacheIdentifier getCacheIdentifier() {
        return RedisCacheIdentifier.KEYWORD_CONFIG;
    }

    @Override
    protected DisplayLabel populateFromDB() {
        DisplayLabel displayLabel;
        try {
            displayLabel = preferenceClient.fetchLabels(TConstants.KEYWORD_CONFIG_GLOBAL_ASSET_TYPE).getData();
        } catch (Exception exception) {
            log.error("Exception while fetching keyword config: " + exception.getMessage());
            return null;
        }
        return displayLabel;
    }

    public static String getDisplayTextByFieldLabel(DisplayLabel displayLabel, String label){
        String displayText = null;
        Map<String, String> displayTextByLabelMap = displayLabel.getDisplayTextByLabel();
        if(Objects.nonNull(displayTextByLabelMap)){
            displayText = displayTextByLabelMap.get(label);
        }
        return displayText;
    }
}
