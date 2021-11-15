package com.tekion.accounting.fs.common.excelGeneration.dto;

import com.tekion.accounting.fs.common.excelGeneration.enums.SupportedFormatOverrideIdentifiers;
import com.tekion.accounting.fs.common.excelGeneration.enums.OverrideType;
import lombok.Data;

@Data
public class ExcelFieldFormattingConfig {
	private SupportedFormatOverrideIdentifiers formatOverrideIdentifiers;
	private OverrideType overrideType;
}