package com.tekion.accounting.fs.common.excelGeneration.dto.financialStatement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FinancialStatementRequestDto {
    @NotBlank
    private String fsId;
    @NotNull(groups = {FSTemplateRequestValidationGroup.class})
    private long tillEpoch;
    private boolean includeM13=true;
    private FinancialYearType financialYearType;
    private boolean addM13BalInDecBalances;
}
