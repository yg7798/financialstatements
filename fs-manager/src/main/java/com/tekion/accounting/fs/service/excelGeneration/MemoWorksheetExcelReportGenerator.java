package com.tekion.accounting.fs.service.excelGeneration;

import com.tekion.accounting.fs.service.common.excelGeneration.reportRows.MemoWorksheetReportRow;
import com.tekion.accounting.fs.enums.AccountingError;
import com.tekion.accounting.fs.service.common.excelGeneration.abstractExecutors.AccAbstractExcelReportGeneratorV2;
import com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs.financialStatment.MemoWorksheetColumnConfig;
import com.tekion.accounting.fs.service.common.excelGeneration.dto.AccExcelRequestDto;
import com.tekion.accounting.fs.service.common.excelGeneration.dto.SheetInfoDto;
import com.tekion.accounting.fs.service.common.excelGeneration.enums.ExcelReportSheet;
import com.tekion.accounting.fs.service.common.excelGeneration.enums.ExcelReportType;
import com.tekion.accounting.fs.service.common.excelGeneration.generators.financialStatement.dto.MemoWorksheetExcelRequestDto;
import com.tekion.accounting.fs.service.common.excelGeneration.generators.financialStatement.dto.MemoWorksheetRequestDto;
import com.tekion.accounting.fs.common.utils.JsonUtil;
import com.tekion.accounting.fs.common.validation.NotNullGroup;
import com.tekion.core.excelGeneration.models.model.ColumnConfig;
import com.tekion.core.excelGeneration.models.model.Sort;
import com.tekion.core.excelGeneration.models.model.v2.ExcelGenerationRequestDtoV2;
import com.tekion.core.excelGeneration.models.model.v2.FetchNextBatchRequestV2;
import com.tekion.core.excelGeneration.models.model.v2.NextBatchDataV2;
import com.tekion.core.excelGeneration.models.model.v2.SheetDetails;
import com.tekion.core.excelGeneration.models.utils.TCollectionUtils;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.validation.TValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class MemoWorksheetExcelReportGenerator extends AccAbstractExcelReportGeneratorV2 {

	private final MemoWorksheetExcelHelperService helperService;
	private final TValidator validator;

	@Override
	public ExcelGenerationRequestDtoV2 createExcelGenerationDto(AccExcelRequestDto requestDto, String reportType) {

		ExcelGenerationRequestDtoV2 excelGenerationRequestDtoV2 = new ExcelGenerationRequestDtoV2();
		populateDefaultsInRequestDto(excelGenerationRequestDtoV2, reportType);

		MemoWorksheetRequestDto memoWorksheetRequestDto = JsonUtil.initializeFromJson(JsonUtil.toJson(requestDto.getRequestDetails()), MemoWorksheetRequestDto.class);
		validateWorksheetExcelGenerationRequestDto(memoWorksheetRequestDto);

		excelGenerationRequestDtoV2.setReportFileName(requestDto.getReportFileName());

		List<ExcelReportSheet> excelReportSheets = ExcelReportSheet.getReportTypeToGroupInfoHolder().get(reportType);
		if(TCollectionUtils.isEmpty(excelReportSheets)){
			throw new TBaseRuntimeException(AccountingError.excelSheetNotRegistered, reportType);
		}
		List<ColumnConfig> columnConfigList = getDefaultColumnConfigList(reportType);

		final SheetDetails sheetDetail = ExcelReportSheet.toUnPaginatedSheetDetails(excelReportSheets.get(0));
		setSortDetails(memoWorksheetRequestDto, sheetDetail);
		sheetDetail.setColumnConfigList(columnConfigList);

		Set<SheetInfoDto> currHashSet = helperService.setSheetIdentifier(sheetDetail);
		MemoWorksheetExcelRequestDto reportRequestDto = new MemoWorksheetExcelRequestDto();
		reportRequestDto.setSheetInfoDtoSet(currHashSet);
		reportRequestDto.setMemoWorksheetRequestDto(memoWorksheetRequestDto);
		reportRequestDto.setTimeStampOfGeneration(System.currentTimeMillis());

		excelGenerationRequestDtoV2.getRequestDetails().setBody(reportRequestDto);
		excelGenerationRequestDtoV2.setSheetDetails(Arrays.asList(sheetDetail));
		return excelGenerationRequestDtoV2;
	}

	private void validateWorksheetExcelGenerationRequestDto(MemoWorksheetRequestDto memoWorksheetRequestDto) {
		if (Objects.isNull(memoWorksheetRequestDto)) {
			throw new TBaseRuntimeException(AccountingError.emptyExcelRequestDto, ExcelReportType.MEMO_WORKSHEET.name());
		}
		validator.validate(memoWorksheetRequestDto, NotNullGroup.class);
	}

	private void setSortDetails(MemoWorksheetRequestDto memoWorksheetRequestDto, SheetDetails sheetDetail) {
		List<Sort> sortListToSet = Lists.newArrayList();
		sortListToSet.addAll(TCollectionUtils.nullSafeList(memoWorksheetRequestDto.getSortList()));

		if(TCollectionUtils.isEmpty(sortListToSet)){
			sortListToSet.add(Sort.builder()
					.key(MemoWorksheetColumnConfig.FS_PAGE.getSortKeyMapping())
					.order(Sort.Order.ASC)
					.build());
		}
		sheetDetail.setSortColumnKeyList(sortListToSet);
	}

	@Override
	public NextBatchDataV2 doOnFetchMoreRecordsCallback(FetchNextBatchRequestV2 fetchNextBatchRequestV2, String reportType) {
		WorksheetExcelReportContext context = new WorksheetExcelReportContext();
		context.setReportType(reportType);
		context.setNextBatchRequestV2(fetchNextBatchRequestV2);

		NextBatchDataV2 nextBatchDataV2 = new NextBatchDataV2();
		switch (fetchNextBatchRequestV2.getDataFetchType()){
			case DATA_RECORDS:
				List<MemoWorksheetReportRow> memoWorksheetReportRows = helperService.getExportableReportRows(reportType,context);
				Map<String, SheetInfoDto> sheetInfoDtoMap = TCollectionUtils.transformToMap(context.getReportRequestDto().getSheetInfoDtoSet(), SheetInfoDto::getSheetIdentifier);
				SheetInfoDto sheetInfoDto = sheetInfoDtoMap.get(fetchNextBatchRequestV2.getSheetIdentifier());
				nextBatchDataV2.setRowDataList(helperService.getSingleRowDataList(memoWorksheetReportRows, sheetInfoDto));
				break;
			case TOP_ADDITIONAL_ROWS:
			case BOTTOM_ADDITIONAL_ROWS:
				nextBatchDataV2.setRowDataList(Lists.newArrayList());
				break;
		}
		return nextBatchDataV2;
	}

	@Override
	public List<String> supportedReportNames() {
		return Arrays.asList(ExcelReportType.MEMO_WORKSHEET.name());
	}

	@Override
	protected List<SheetDetails> getSheetDetailsForReport(String reportType) {
		return null;
	}

}
