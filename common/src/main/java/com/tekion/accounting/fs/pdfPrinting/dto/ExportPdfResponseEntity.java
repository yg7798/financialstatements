package com.tekion.accounting.fs.pdfPrinting.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties
public class ExportPdfResponseEntity {
    private String status;
    private Object data;
}
