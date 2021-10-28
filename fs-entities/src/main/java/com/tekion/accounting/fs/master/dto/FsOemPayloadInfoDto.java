package com.tekion.accounting.fs.master.dto;


import com.tekion.accounting.fs.master.beans.Detail;
import com.tekion.accounting.fs.master.beans.FsOemPayloadInfo;
import com.tekion.accounting.fs.master.enums.OEM;
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
