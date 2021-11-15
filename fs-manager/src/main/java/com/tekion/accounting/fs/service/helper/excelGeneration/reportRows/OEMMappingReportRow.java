package com.tekion.accounting.fs.service.helper.excelGeneration.reportRows;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OEMMappingReportRow {

    public static final String ACTIVE = "Active";
    public static final String INACTIVE = "Inactive";

    private String franchise;
    private String status;
    private String glAccountNumber;
    private String glAccountName;
    private String accountStatus;
    private String accountType;
    private String department;
    private BigDecimal ytdBalance = BigDecimal.ZERO;
    private BigDecimal mtdBalance = BigDecimal.ZERO;
    private Long mtdCount;
    private Long ytdCount;
    private String groupCodes;

}