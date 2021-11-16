package com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tekion.accounting.fs.service.common.excelGeneration.enums.ExcelFieldIdentifier;
import com.tekion.accounting.fs.service.common.excelGeneration.enums.SupportedFormatOverrideIdentifiers;
import com.tekion.core.excelGeneration.models.model.ColumnConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@Builder(builderMethodName = "buildAccountColumnConfig")
@NoArgsConstructor
public class AccColumnConfig extends ColumnConfig {
    private SupportedFormatOverrideIdentifiers formatOverrideIdentifier;
    private ExcelFieldIdentifier excelFieldIdentifier;
    private String preferenceColumnKey;
}
