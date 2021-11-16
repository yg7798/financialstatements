package com.tekion.accounting.fs.service.common.excelGeneration.dto;

import com.tekion.accounting.fs.service.common.excelGeneration.enums.SupportedFormatOverrideIdentifiers;
import com.tekion.accounting.fs.service.common.excelGeneration.enums.OverrideType;
import lombok.Data;

@Data
public class ExcelFieldFormattingConfig {
	private SupportedFormatOverrideIdentifiers formatOverrideIdentifiers;
	private OverrideType overrideType;
}