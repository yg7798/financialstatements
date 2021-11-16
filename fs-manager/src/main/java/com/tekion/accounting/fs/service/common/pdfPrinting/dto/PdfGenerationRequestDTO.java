package com.tekion.accounting.fs.service.common.pdfPrinting.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class PdfGenerationRequestDTO {
    private String assetId;


}
