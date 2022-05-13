package com.tekion.accounting.fs.service.utils;

import com.tekion.accounting.fs.common.utils.UserContextUtils;
import com.tekion.dealersettings.dealermaster.beans.DealerMaster;
import com.tekion.dealersettings.dealermaster.beans.SiteOverride;
import lombok.experimental.UtilityClass;
import java.util.Objects;

@UtilityClass
public class FSDealerMasterUtils {

    public static DealerMaster getDealerMasterInfo(Boolean overrideSiteInfo, DealerMaster dealerMaster){
        if(Boolean.TRUE.equals(overrideSiteInfo)
                    && Objects.nonNull(dealerMaster.getOemSiteOverrideMap())
                        && Objects.nonNull(dealerMaster.getOemSiteOverrideMap().get(UserContextUtils.getSiteIdFromUserContext()))) {
            SiteOverride siteInfo = dealerMaster.getOemSiteOverrideMap().get(UserContextUtils.getSiteIdFromUserContext());
            return DealerMaster.builder()
                    .dealerName(siteInfo.getDealerName())
                    .dealerAddress(siteInfo.getDealerAddress())
                    .dealerEmail(siteInfo.getDealerEmail())
                    .timeZone(siteInfo.getTimeZone())
                    .oemDetails(siteInfo.getOemDetails())
                    .dealerEmail(siteInfo.getDealerEmail())
                    .bar(siteInfo.getBar())
                    .dealerLogos(siteInfo.getDealerLogos())
                    .dealershipCode(siteInfo.getDealershipCode())
                    .website(siteInfo.getWebsite())
                    .phone(siteInfo.getPhone())
                    .epa(siteInfo.getEpa())
                    .dealerDoingBusinessAsName(siteInfo.getDealerDoingBusinessAsName())
                    .makeCode(siteInfo.getMakeCode())
                    .taxRegimeConfig(siteInfo.getTaxRegimeConfig())
                    .privacyPolicy(siteInfo.getPrivacyPolicy())
                    .taxConfig(siteInfo.getTaxConfig())
                    .integratedDms(siteInfo.getIntegratedDms())
                    .supportedOEMs(siteInfo.getSupportedOEMs())
                    .supportedMakes(siteInfo.getSupportedMakes())
                    .dealerOperationSchedule(siteInfo.getDealerOperationSchedule())
                    .build();
        }
        return dealerMaster;
    }

}
