package com.tekion.accounting.fs.excelGeneration.generators.financialStatement.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tekion.accounting.fs.excelGeneration.dto.AccReportCallbackDtoV2;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemoWorksheetExcelRequestDto extends AccReportCallbackDtoV2 {
    private MemoWorksheetRequestDto memoWorksheetRequestDto;
}
