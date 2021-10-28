package com.tekion.accounting.fs.master.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.tekion.accounting.fs.master.beans.*;

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
