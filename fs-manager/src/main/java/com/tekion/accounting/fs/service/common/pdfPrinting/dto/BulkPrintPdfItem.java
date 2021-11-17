package com.tekion.accounting.fs.service.common.pdfPrinting.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkPrintPdfItem {
    public static int DEFAULT_PRINT_COPIES = 1;
    public static long DEFAULT_PAGE_START = -1L;
    public static long DEFAULT_PAGE_END = 1L;
    public static boolean DEFAULT_DUPLEX = false;

    private String url;

    @Builder.Default
    private int copies = DEFAULT_PRINT_COPIES;
    @Builder.Default
    private boolean duplex = DEFAULT_DUPLEX;
    @Builder.Default
    private Long pageStart = DEFAULT_PAGE_START;
    @Builder.Default
    private Long pageEnd  = DEFAULT_PAGE_END;
}
