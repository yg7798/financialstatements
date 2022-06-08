package com.tekion.accounting.fs.service.utils;

import com.tekion.accounting.commons.dealer.DealerConfig;
import com.tekion.clients.dealerproperty.DealerPropertyStore;
import com.tekion.core.utils.TStringUtils;
import com.tekion.core.utils.UserContextProvider;
import com.tekion.multilingual.dto.TekMultiLingualBean;
import com.tekion.tekionconstant.locale.TekLocale;
import lombok.experimental.UtilityClass;

import java.util.Objects;
@UtilityClass
public class FSLocaleUtils {

    public static String getLocale(DealerPropertyStore dealerPropertyStore, DealerConfig dealerConfig){
        // Will uncomment this code to fetch locale from dealer property and dealer-master
        //return FSDealerPropertyUtils.getLocaleBasedOnDealerProperty(dealerPropertyStore, dealerConfig);
        if(Objects.isNull(UserContextProvider.getCurrentLocale())) {
            return TekLocale.en.name() + "_" + dealerConfig.getDealerCountryCode();
        }
        return UserContextProvider.getCurrentLocale().name();
    }

    public static String getTranslatedValue(TekMultiLingualBean languages, String key, String defaultValue) {
        String localeValue;
        if(Objects.nonNull(UserContextProvider.getCurrentLocale())) {
           TekLocale locale = UserContextProvider.getCurrentLocale();
            if(Objects.nonNull(languages)
                    && Objects.nonNull(languages.getLocale())
                    && Objects.nonNull(languages.getLocale().get(locale))) {
                localeValue = (String) languages.getLocale().get(locale).get(key);
                if(TStringUtils.isNotBlank(localeValue))
                    return localeValue;
            }
        }
            return defaultValue;
    }
}
