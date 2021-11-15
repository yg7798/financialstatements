package com.tekion.accounting.fs.dto;

import com.google.common.collect.Maps;
import com.tekion.accounting.fs.beans.common.AccountingOemFsCellCode;
import com.tekion.accounting.fs.dto.cellcode.FsCodeDetail;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Builder
@Data
public class FSResponse {
    @Builder.Default private Map<String, FsCodeDetail> codeVsDetailsMap = Maps.newHashMap();
    private List<AccountingOemFsCellCode> accountingOemFsCellCodes;
}
