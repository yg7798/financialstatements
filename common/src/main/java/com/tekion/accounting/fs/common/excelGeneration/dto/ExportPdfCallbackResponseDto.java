package com.tekion.accounting.fs.common.excelGeneration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class ExportPdfCallbackResponseDto {
	private String status;
	private PdfExportMetadata metaData;
	private String mediaId;
}
