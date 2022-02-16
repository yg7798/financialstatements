package com.tekion.accounting.fs.dto.accountingInfo;

import com.tekion.accounting.fs.beans.accountingInfo.AccountingInfo;
import com.tekion.accounting.fs.beans.accountingInfo.FSPreferences;
import com.tekion.accounting.fs.beans.accountingInfo.FsOemPayloadInfo;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.UserContextProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountingInfoDto {
    private boolean bsdPresent;
    private boolean nodPresent;
    private Boolean fsRoundOffOffset;
    private List<FsOemPayloadInfoDto> oemPayloadInfos;
    private Set<String> supportedOEMs = new HashSet<>();
    private String primaryOEM;
    private String salesCode;
    private String salesZoneCode;
    private String pnaCode;
    private FSPreferences fsPreferences;
    private Map<String, String> oemToOffsetCellMap = new HashMap<>();

    public AccountingInfo toAccountingInfo(){
        AccountingInfo info = new AccountingInfo();
        info.setBsdPresent(bsdPresent);
        info.setNodPresent(nodPresent);
        info.setTenantId(UserContextProvider.getCurrentTenantId());
        info.setFsRoundOffOffset(fsRoundOffOffset);
        info.setSupportedOEMs(supportedOEMs);
        info.setPrimaryOEM(primaryOEM);
        info.setSalesCode(salesCode);
        info.setSalesZoneCode(salesZoneCode);
        info.setPnaCode(pnaCode);
        info.setFsPreferences(fsPreferences);
        info.setOemToOffsetCellMap(oemToOffsetCellMap);

        if(TCollectionUtils.isNotEmpty(oemPayloadInfos)){
            List<FsOemPayloadInfo> l = new ArrayList<>();
            for(FsOemPayloadInfoDto dto: oemPayloadInfos){
                l.add(dto.toFsOemPayloadInfo());
            }
            info.setOemPayloadInfos(l);
        }

        return info;
    }
}
