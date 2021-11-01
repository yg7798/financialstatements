package com.tekion.accounting.fs.pdfPrinting.dto;

import lombok.Data;

import java.util.List;

@Data
public class BulkExportCallBackDto {
    private String requestId;
    private List<BulkMediaItemsDto> documents;
}
