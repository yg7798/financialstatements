package com.tekion.accounting.fs.api;

import com.tekion.accounting.fs.dto.oemPayload.FinancialReportRequestBody;
import com.tekion.accounting.fs.dto.oemPayload.FinancialStatementRequestDto;
import com.tekion.accounting.fs.service.oemPayload.FSReportServiceImpl;
import com.tekion.accounting.fs.service.oemPayload.FsXMLServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/financialStatement")
@RequiredArgsConstructor
public class FinancialStatementApi {

	private final FsXMLServiceImpl financialStatementService;
	private final FSReportServiceImpl fsReportService;
	private final FsXMLServiceImpl xmlService;


	@PostMapping(value = "/",produces = MediaType.TEXT_XML_VALUE)
	public String getFinancialStatement(@RequestBody FinancialReportRequestBody financialReportRequestBody, HttpServletResponse response) {
		return financialStatementService.getFinancialStatement(financialReportRequestBody);
	}

	@PostMapping(value = "/xml", produces = {"application/xml"} )
	public String getFSasXML(@RequestBody FinancialStatementRequestDto financialStatementRequestDto){
		return fsReportService.generateXML(financialStatementRequestDto);
	}


	@PostMapping(value = "/submit" )
	public ResponseEntity submit(@RequestBody FinancialStatementRequestDto financialStatementRequestDto){
		return ResponseEntity.ok(fsReportService.submit(financialStatementRequestDto));
	}



	@PostMapping(value = "/downloadFile" )
	public void downloadFile(@RequestBody FinancialStatementRequestDto financialStatementRequestDto,
							 HttpServletResponse response){
		fsReportService.downloadFile(financialStatementRequestDto, response);
	}


	@PostMapping(value = "/statement" )
	public ResponseEntity getFS(@RequestBody FinancialStatementRequestDto financialStatementRequestDto){
		return ResponseEntity.ok(fsReportService.getStatement(financialStatementRequestDto));
	}


}
