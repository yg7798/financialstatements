package com.tekion.accounting.fs.cache.redis.implementation;


import com.tekion.accounting.fs.cache.redis.enums.RedisCacheIdentifier;
import com.tekion.accounting.fs.cache.redis.helper.AbstractRedisCache;
import com.tekion.clients.preference.beans.DisplayLabel;
import com.tekion.clients.preference.client.PreferenceClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

import static com.tekion.accounting.fs.TConstants.KEYWORD_CONFIG_GLOBAL_ASSET_TYPE;


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
            displayLabel = preferenceClient.fetchLabels(KEYWORD_CONFIG_GLOBAL_ASSET_TYPE).getData();
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
