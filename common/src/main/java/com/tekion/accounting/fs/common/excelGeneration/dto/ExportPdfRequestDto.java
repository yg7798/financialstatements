package com.tekion.accounting.fs.common.excelGeneration.dto;

import com.tekion.accounting.fs.common.TConstants;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExportPdfRequestDto {
	private PdfExportUrlParamsRequest urlParams;
	private String documentType;
	private final String microService = TConstants.ACCOUNTING_SMALL_CASE;
	private PdfExportMetadata metaData;
	private String callbackUrl;
}
