package com.tekion.accounting.fs.dto.cellcode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;
import com.tekion.accounting.fs.beans.common.AccountingOemFsCellCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FsCellCodeDetailsResponseDto {

    @Builder.Default
    private Map<String, FsCodeDetail> codeVsDetailsMap = Maps.newHashMap();
    @JsonIgnore
    private List<AccountingOemFsCellCode> accountingOemFsCellCodes;

}
