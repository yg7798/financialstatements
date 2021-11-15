package com.tekion.accounting.fs.dto;


import com.tekion.accounting.fs.integration.Detail;
import com.tekion.accounting.fs.beans.accountingInfo.FsOemPayloadInfo;
import com.tekion.accounting.fs.enums.OEM;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FsOemPayloadInfoDto {
    private OEM oem;
    private String durationType;
    private List<Detail> details;

    public FsOemPayloadInfo toFsOemPayloadInfo(){
        return FsOemPayloadInfo.builder().oem(oem.name()).durationType(durationType).details(details).build();
    }
}
