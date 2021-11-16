package com.tekion.accounting.fs.service.common.excelGeneration.reportRows;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemoWorksheetReportRow {

    private String fsPage;
    private String fsLine;
    private String description;
    private String status;
    private String mtdValue;
    private String ytdValue;
    private String fieldType;
    private boolean isYtdEnabled;
}