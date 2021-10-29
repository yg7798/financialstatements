package com.tekion.accounting.fs.beans.accountingInfo;

import com.tekion.accounting.fs.beans.integration.Detail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FsOemPayloadInfo {
    private String oem;
    private String durationType;
    // accountId vs Value
    private List<Detail> details;
}
