package com.tekion.accounting.fs.common.excelGeneration.apiGateway.dto;

import lombok.Data;

@Data
public class PdfPreviewResponseDto {
    private String mediaId;
    private Object originalRequestDto;
    private String jobId;

}
