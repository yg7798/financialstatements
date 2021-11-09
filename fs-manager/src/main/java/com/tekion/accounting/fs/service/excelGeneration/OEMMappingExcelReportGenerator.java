package com.tekion.accounting.fs.service.excelGeneration;

import com.tekion.accounting.fs.excelGeneration.abstractExecutors.AccAbstractExcelReportGenerator;
import com.tekion.accounting.fs.excelGeneration.dto.AccExcelRequestDto;
import com.tekion.accounting.fs.excelGeneration.dto.financialStatement.OEMMappingRequestDto;
import com.tekion.accounting.fs.excelGeneration.enums.ExcelReportType;
import com.tekion.accounting.fs.excelGeneration.helper.ExcelReportGeneratorHelper;
import com.tekion.accounting.fs.excelGeneration.reportRows.OEMMappingReportRow;
import com.tekion.accounting.fs.utils.JsonUtil;
import com.tekion.accounting.fs.validation.NotNullGroup;
import com.tekion.accounting.fs.validation.RangeValidatorGroup;
import com.tekion.core.excelGeneration.models.model.ExcelGenerationRequestDto;
import com.tekion.core.excelGeneration.models.model.FetchNextBatchRequest;
import com.tekion.core.excelGeneration.models.model.NextBatchData;
import com.tekion.core.validation.TValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OEMMappingExcelReportGenerator extends AccAbstractExcelReportGenerator {

	private final OEMMappingExcelHelperService helperService;
	private final ExcelReportGeneratorHelper helper;
	private final TValidator validator;

	@Override
	protected ExcelGenerationRequestDto createExcelGenerationDto(AccExcelRequestDto requestDto, String reportType) {
		ExcelGenerationRequestDto excelGenerationRequestDtoForClient = new ExcelGenerationRequestDto();
		populateDefaultsInRequestDto(excelGenerationRequestDtoForClient, reportType);

		OEMMappingRequestDto oemMappingRequestDto = JsonUtil.initializeFromJson(JsonUtil.toJson(requestDto.getRequestDetails()), OEMMappingRequestDto.class);
		validator.validate(oemMappingRequestDto, RangeValidatorGroup.class, NotNullGroup.class);
		com.tekion.as.excelGenearation.dto.ESReportCallbackDto reportCallbackDto = new com.tekion.as.excelGenearation.dto.ESReportCallbackDto();
		reportCallbackDto.setExtraInfoForCallback(oemMappingRequestDto);
		reportCallbackDto.setMinimizedResourceMetaData(getMinimizeMetadata(excelGenerationRequestDtoForClient));

		excelGenerationRequestDtoForClient.setSortColumnKeyList(helper.createSortToBeDoneOnLambda( reportType, oemMappingRequestDto,false));
		excelGenerationRequestDtoForClient.setReportType(reportType);

		excelGenerationRequestDtoForClient.setPaginatedCall(false);
		excelGenerationRequestDtoForClient.getRequestDetails().setBody(reportCallbackDto);
		excelGenerationRequestDtoForClient.setReportFileName(requestDto.getReportFileName());

		return excelGenerationRequestDtoForClient;

	}

	@Override
	public NextBatchData doOnFetchMoreRecordsCallback(FetchNextBatchRequest fetchNextBatchRequest, String reportType) {
		OEMMappingExcelReportContext context = new OEMMappingExcelReportContext();
		context.setFetchNextBatchRequest(fetchNextBatchRequest);

		List<OEMMappingReportRow> exportableReportRows = helperService.getExportableReportRows(ExcelReportType.valueOf(reportType), context);
		NextBatchData nextBatchData = new NextBatchData();
		List<Object> objectList = Lists.newArrayList();
		objectList.addAll(exportableReportRows);
		nextBatchData.setObjList(objectList);
		return nextBatchData;
	}

	@Override
	public List<String> supportedReportNames() {
		return Arrays.asList(ExcelReportType.OEM_MAPPING.name());
	}
}

