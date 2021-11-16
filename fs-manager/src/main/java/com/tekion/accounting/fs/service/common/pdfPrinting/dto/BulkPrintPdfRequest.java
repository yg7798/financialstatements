package com.tekion.accounting.fs.service.common.pdfPrinting.dto;

import com.tekion.admin.beans.Department;
import com.tekion.admin.beans.printer.FormModule;
import com.tekion.printer.beans.tcp.PrinterOptions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BulkPrintPdfRequest {

    @Builder.Default
    private Department department = Department.ACCOUNTING;

    private FormModule formModule;
    private String userId;
    private PrinterOptions options;

    private List<BulkPrintPdfItem> paths;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Options {
        public String paperSize;
        public String trayId;
    }
}
