package com.tekion.accounting.fs.service.printing;

import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.service.printing.models.FSViewStatementDto;
import com.tekion.accounting.fs.service.common.pdfPrinting.PDFPrintService;
import com.tekion.accounting.fs.service.common.pdfPrinting.dto.BulkPdfRequest;
import com.tekion.accounting.fs.service.common.pdfPrinting.dto.PdfDocumentType;
import com.tekion.accounting.fs.service.common.pdfPrinting.dto.PdfStatus;
import com.tekion.core.utils.UserContext;
import com.tekion.core.utils.UserContextProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class FSPrintServiceImpl implements FSPrintService {

	private final PDFPrintService pdfPrintService;

	@Override
	public Object viewStatement(FSViewStatementDto dto) {
		String requestId = UUID.randomUUID().toString();
		Map<String, Object> params = dto.getParams();
		return pdfExportForViewing(requestId, UserContextProvider.getContext(), params);
	}

	private Object pdfExportForViewing(String requestId, UserContext context, Map<String, Object> params) {
		try {
			BulkPdfRequest bulkPdfRequest = createPdfRequestForViewing(requestId, params);
			return pdfPrintService.exportBulkPdfWithResponse(bulkPdfRequest, context);
		} catch (Exception exp) {
			log.info("Exception while exporting FS pdf requestId : {}", requestId);
			return null;
		}
	}

	private BulkPdfRequest createPdfRequestForViewing(String requestId, Map<String, Object> params) {
		BulkPdfRequest pdfRequest = new BulkPdfRequest();
		pdfRequest.setId(requestId);
		pdfRequest.setAssetType(TConstants.PDF_ASSET_TYPE);
		pdfRequest.setStatus(PdfStatus.REQUESTED.name());
		pdfRequest.setDocumentType(PdfDocumentType.FINANCIAL_STATEMENT.name());
		pdfRequest.setRespondWithMediaUrls(true);
		pdfRequest.setExtras(params);
		return pdfRequest;
	}
}
