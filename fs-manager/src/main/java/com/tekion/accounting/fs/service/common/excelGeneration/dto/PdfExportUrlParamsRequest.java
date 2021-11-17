package com.tekion.accounting.fs.service.common.excelGeneration.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PdfExportUrlParamsRequest {
	private String mediaId;
	private String groupByKey;
	private String subGroupKey;
	private String reportId;
	private String assetType;
}
