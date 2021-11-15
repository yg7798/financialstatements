package com.tekion.accounting.fs.service.helper.excelGeneration.dto;

import com.tekion.accounting.fs.service.helper.excelGeneration.enums.SupportedFormatOverrideIdentifiers;
import com.tekion.accounting.fs.service.helper.excelGeneration.enums.OverrideType;
import lombok.Data;

@Data
public class ExcelFieldFormattingConfig {
	private SupportedFormatOverrideIdentifiers formatOverrideIdentifiers;
	private OverrideType overrideType;
}