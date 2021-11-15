package com.tekion.accounting.fs.service.excelGeneration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.dto.request.FinancialStatementRequestDto;
import com.tekion.accounting.fs.service.helper.excelGeneration.abstractExecutors.AccAbstractTemplateReportGenerator;
import com.tekion.accounting.fs.service.helper.excelGeneration.dto.AccExcelRequestDto;
import com.tekion.accounting.fs.service.helper.excelGeneration.dto.financialStatement.FSTemplateRequestValidationGroup;
import com.tekion.accounting.fs.service.helper.excelGeneration.enums.ExcelReportType;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.service.oemPayload.FinancialStatementService;
import com.tekion.accounting.fs.common.template.TemplateService;
import com.tekion.accounting.fs.common.utils.JsonUtil;
import com.tekion.core.excelGeneration.models.model.template.ExcelTemplateBatchData;
import com.tekion.core.excelGeneration.models.model.template.ExcelTemplateRequestDto;
import com.tekion.core.excelGeneration.models.model.template.FetchTemplateDataRequest;
import com.tekion.core.excelGeneration.models.model.template.SingleCellData;
import com.tekion.core.utils.UserContextProvider;
import com.tekion.core.validation.TValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class FinancialStatementTemplateReportGenerator extends AccAbstractTemplateReportGenerator {

	@Autowired
	protected TemplateService templateService;

	@Autowired
	private FinancialStatementService financialStatementService;

	@Autowired
	private TValidator validator;
	@Autowired
	private FSEntryRepo fsEntryRepo;

	@Override
	public List<String> supportedReportNames() {
		return Arrays.asList(ExcelReportType.FINANCIAL_STATEMENT.name());
	}

	@Override
	protected ExcelTemplateRequestDto createExcelTemplateDto(AccExcelRequestDto payload, String reportType) {
		FinancialStatementRequestDto statementRequestDto = JsonUtil.initializeFromJson(
				JsonUtil.toJson(payload.getRequestDetails()), FinancialStatementRequestDto.class);
		validator.validate(statementRequestDto, FSTemplateRequestValidationGroup.class);

		FSEntry fsEntry = fsEntryRepo.findByIdAndDealerIdWithNullCheck(statementRequestDto.getFsId(), UserContextProvider.getCurrentDealerId());
		String templateUrl = templateService.getMediaPathForFSTemplate(fsEntry.getOemId(), fsEntry.getYear());
		ExcelTemplateRequestDto templateRequestDto = new ExcelTemplateRequestDto();
		templateRequestDto.setTemplateUrl(templateUrl);
		populateDefaultsInRequestDto(templateRequestDto, reportType);
		templateRequestDto.getRequestDetails().setBody(statementRequestDto);
		templateRequestDto.setReportFileName(payload.getReportFileName());
		return templateRequestDto;
	}

	@Override
	public ExcelTemplateBatchData doOnFetchMoreRecordsCallback(FetchTemplateDataRequest request, String reportName) {
		try {
			ObjectMapper mapper = JsonUtil.MAPPER;
			FinancialStatementRequestDto requestDto = mapper.convertValue(
					request.getOriginalPayload(), FinancialStatementRequestDto.class);
			List<SingleCellData> cellDataList = financialStatementService.getCellLevelFSReportData(requestDto);
			ExcelTemplateBatchData templateResponse = new ExcelTemplateBatchData();
			templateResponse.setCellDataList(cellDataList);
			return templateResponse;
		} catch (ClassCastException e) {
			log.error("Casting exception while fetching template data {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			log.error("unknown error occured in doOnFetchMoreRecordsCallback for template {}", e.getMessage());
			throw e;
		}
	}
}


