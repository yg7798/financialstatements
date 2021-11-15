package com.tekion.accounting.fs.service.helper.excelGeneration.helper;

import com.tekion.accounting.fs.service.helper.excelGeneration.dto.ExportPdfCallbackResponseDto;
import com.tekion.accounting.fs.service.helper.excelGeneration.enums.ExcelReportSheet;
import com.tekion.accounting.fs.service.helper.excelGeneration.enums.ExcelReportType;
import com.tekion.core.excelGeneration.models.model.MediaUploadResponse;
import com.tekion.core.excelGeneration.models.model.SuccessCallbackResponse;
import com.tekion.core.service.internalauth.AbstractServiceClientFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PdfExportReportHelper {
    private static final String PDF = ".pdf";
    private final ExcelReportGeneratorHelperImpl excelReportGeneratorHelper;
    private final AbstractServiceClientFactory clientFactory;

    public void sendNotificationForPDF(ExcelReportType reportType, ExportPdfCallbackResponseDto exportPdfCallbackResponseDto) {
        log.info("Sending notification for pdf {} {}",
                exportPdfCallbackResponseDto.getMetaData().getParentRequestId(), reportType);
        final String fileName = ExcelReportSheet.getReportTypeToGroupInfoHolder().get(reportType.name()).get(0).getSheetName();
        final SuccessCallbackResponse successCallbackResponse = SuccessCallbackResponse.builder()
                .mediaUploadResponse(MediaUploadResponse.builder()
                        .mediaId(exportPdfCallbackResponseDto.getMediaId())
                        .build())
                .reportOriginalFileName(fileName + PDF)
                .build();
        excelReportGeneratorHelper.sendNotification(reportType, successCallbackResponse);
    }

//    public void sendRequestToPdfExport(ExportPdfRequestDto exportPdfRequestDto) throws IOException {
//        final String csInstanceUrl = clientFactory.getServiceBaseUrl(TConstants.ACCOUNTING);
//        exportPdfRequestDto.setCallbackUrl(csInstanceUrl + exportPdfRequestDto.getCallbackUrl());
//        final String pdfExportServiceBaseUrl = System.getenv("pdf_export_service_url");
//        ExternalCallUtils.makePostCall(ExternalCallUtils.getHeaders(TRequestUtils.userCallHeaderMap()), pdfExportServiceBaseUrl + "/exports/r/bulkReport/pdf", exportPdfRequestDto);
//    }
}
