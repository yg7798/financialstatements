package com.tekion.accounting.fs.master.beans;

import com.tekion.core.beans.TBaseMongoBean;
import com.tekion.core.utils.UserContextProvider;
import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class AccountingInfo extends TBaseMongoBean {

    private static final String SALES_CODE = "salesCode";
    private static final String SALES_ZONE_CODE = "salesZoneCode";
    private static final String PNA_CODE = "pnaCode";
    private static final String YTD_TERM = "ytdTerm";
    private static final String MTD_TERM = "mtdTerm";

    private boolean bsdPresent; // Body shop department
    private boolean nodPresent; // New Other Dep
    private Boolean fsRoundOffOffset;
    private String dealerId;
    private String primaryOEM;
    private Set<String> supportedOEMs = new HashSet<>();
    private String createdByUserId;
    private String modifiedByUserId;
    private List<FsOemPayloadInfo> oemPayloadInfos;
    private String salesCode;
    private String salesZoneCode;
    private String pnaCode;
    private FSPreferences fsPreferences;
    /**
     * oemId, memo key
     * */
    private Map<String, String> oemToOffsetCellMap;

    public AccountingInfo(){
        this.setDealerId(UserContextProvider.getCurrentDealerId());
        this.setCreatedByUserId(UserContextProvider.getCurrentUserId());
        this.setCreatedTime(System.currentTimeMillis());
        this.setModifiedTime(System.currentTimeMillis());
        this.setModifiedByUserId(UserContextProvider.getCurrentUserId());
    }

}
