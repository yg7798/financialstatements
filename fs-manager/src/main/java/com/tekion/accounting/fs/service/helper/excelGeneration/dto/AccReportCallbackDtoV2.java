package com.tekion.accounting.fs.service.helper.excelGeneration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class AccReportCallbackDtoV2 {
    private Set<SheetInfoDto> sheetInfoDtoSet;
    private long timeStampOfGeneration;
}
