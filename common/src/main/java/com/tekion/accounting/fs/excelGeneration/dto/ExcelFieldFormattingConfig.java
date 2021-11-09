package com.tekion.accounting.fs.excelGeneration.dto;

import com.tekion.accounting.fs.excelGeneration.enums.OverrideType;
import com.tekion.accounting.fs.excelGeneration.enums.SupportedFormatOverrideIdentifiers;
import lombok.Data;

@Data
public class ExcelFieldFormattingConfig {
	private SupportedFormatOverrideIdentifiers formatOverrideIdentifiers;
	private OverrideType overrideType;
}