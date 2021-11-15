package com.tekion.accounting.fs.dto;


import com.tekion.accounting.fs.beans.common.OemConfig;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.core.utils.UserContextProvider;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class OemConfigRequestDto {

    @NotNull
    private OEM oemId;
    @NotBlank
    private String country;
    private boolean xmlEnabled;
    private boolean submissionEnabled;
    private String oemLogoURL;
    private String defaultPrecision;
    private boolean useDealerLogo;
    private Map<String, String> additionalInfo;
    private boolean downloadFileFromIntegration;
    private boolean enableRoundOff;
    private boolean enableRoundOffOffset;

    private List<OemConfig.SupportedFileFormats> supportedFileFormats = new ArrayList<>();


    public OemConfig createOemInfo() {
        OemConfig oemConfig = new OemConfig();
        oemConfig.setOemId(oemId.name());
        oemConfig.setCountry(country);
        oemConfig.setOemLogoURL(oemLogoURL);
        oemConfig.setXmlEnabled(xmlEnabled);
        oemConfig.setSubmissionEnabled(submissionEnabled);
        oemConfig.setDefaultPrecision(defaultPrecision);
        oemConfig.setSupportedFileFormats(
            supportedFileFormats.stream().map(OemConfig.SupportedFileFormats::name).collect(Collectors.toList())
        );
        oemConfig.setUseDealerLogo(useDealerLogo);
        oemConfig.setAdditionalInfo(additionalInfo);
        oemConfig.setDownloadFileFromIntegration(downloadFileFromIntegration);
        oemConfig.setEnableRoundOff(enableRoundOff);
        oemConfig.setEnableRoundOffOffset(enableRoundOffOffset);
        oemConfig.setCreatedByUserId(UserContextProvider.getCurrentUserId());
        oemConfig.setModifiedByUserId(UserContextProvider.getCurrentUserId());
        oemConfig.setModifiedTime(System.currentTimeMillis());
        oemConfig.setCreatedTime(System.currentTimeMillis());

        return oemConfig;
    }
}
