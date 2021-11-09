package com.tekion.accounting.fs.excelGeneration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class AccExcelRequestDto {
    private Object requestDetails;

    private String reportFileName;
//    private ExcelReportType excelReportType;

}
