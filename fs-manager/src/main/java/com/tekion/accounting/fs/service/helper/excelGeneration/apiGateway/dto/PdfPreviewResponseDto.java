package com.tekion.accounting.fs.service.helper.excelGeneration.apiGateway.dto;

import lombok.Data;

@Data
public class PdfPreviewResponseDto {
    private String mediaId;
    private Object originalRequestDto;
    private String jobId;

}
