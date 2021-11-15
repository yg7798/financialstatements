package com.tekion.accounting.fs.service.helper.pdfPrinting.dto;

import lombok.Data;

import java.util.List;

@Data
public class BulkExportCallBackDto {
    private String requestId;
    private List<BulkMediaItemsDto> documents;
}
