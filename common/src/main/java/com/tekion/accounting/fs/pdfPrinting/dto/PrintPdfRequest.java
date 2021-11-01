package com.tekion.accounting.fs.pdfPrinting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrintPdfRequest {
    private String macID;
    private String path;
    private Map<String,Object> options;

}
