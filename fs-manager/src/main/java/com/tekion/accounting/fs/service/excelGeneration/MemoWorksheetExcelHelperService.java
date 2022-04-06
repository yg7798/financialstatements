package com.tekion.accounting.fs.service.excelGeneration;


import com.tekion.accounting.commons.utils.LocaleUtils;
import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.beans.memo.MemoValue;
import com.tekion.accounting.fs.beans.memo.MemoWorksheet;
import com.tekion.accounting.fs.beans.memo.MemoWorksheetTemplate;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.enums.OemCellDurationType;
import com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs.enums.ExcelCellFormattingHolder;
import com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs.financialStatment.MemoWorksheetColumnConfig;
import com.tekion.accounting.fs.service.common.excelGeneration.dto.SheetInfoDto;
import com.tekion.accounting.fs.service.common.excelGeneration.generators.financialStatement.dto.MemoWorksheetExcelRequestDto;
import com.tekion.accounting.fs.service.common.excelGeneration.generators.financialStatement.dto.MemoWorksheetRequestDto;
import com.tekion.accounting.fs.service.common.excelGeneration.generators.financialStatement.dto.WorksheetApplicableFilter;
import com.tekion.accounting.fs.service.common.excelGeneration.helper.ExcelReportGeneratorHelper;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.service.worksheet.MemoWorksheetService;
import com.tekion.accounting.fs.service.worksheet.MemoWorksheetTemplateService;
import com.tekion.accounting.fs.common.utils.JsonUtil;
import com.tekion.accounting.fs.service.common.excelGeneration.reportRows.MemoWorksheetReportRow;
import com.tekion.core.excelGeneration.models.model.ColumnConfig;
import com.tekion.core.excelGeneration.models.model.v2.SheetDetails;
import com.tekion.core.excelGeneration.models.model.v2.SingleRowData;
import com.tekion.core.excelGeneration.models.utils.TCollectionUtils;
import com.tekion.core.excelGeneration.models.utils.TStringUtils;
import com.tekion.core.utils.UserContextProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.assertj.core.util.Lists;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.tekion.accounting.fs.common.TConstants.STATUS;
import static com.tekion.accounting.fs.service.common.excelGeneration.reportRows.OEMMappingReportRow.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class MemoWorksheetExcelHelperService {

	private final MemoWorksheetService memoWorksheetService;
	private final MemoWorksheetTemplateService memoWorksheetTemplateService;
	private final ExcelReportGeneratorHelper helper;
	private final FSEntryRepo fsEntryRepo;
	private static String PAGE_NUMBER = "pageNumber";
	private static String LINE_NUMBER = "lineNumber";

	public List<MemoWorksheetReportRow> getExportableReportRows(String  reportType, WorksheetExcelReportContext context) {
		populateDefaultsInContext(context);
		populateWorksheetDataInContext(context);
		List<MemoWorksheetReportRow> memoWorksheetReportRowsCurrFetch = createMemoWorksheetReportRows(context);
		return applySearchAndFiltersOnReportRows(context, memoWorksheetReportRowsCurrFetch);
	}

	public List<MemoWorksheetReportRow> createMemoWorksheetReportRows(WorksheetExcelReportContext context){
		MemoWorksheetRequestDto memoWorksheetRequestDto = context.getReportRequestDto().getMemoWorksheetRequestDto();
		int month = memoWorksheetRequestDto.getMonth();
		List<MemoWorksheetReportRow> memoWorksheetReportRows = new ArrayList<>();
		Map<String, MemoWorksheet> keyToMemoWorksheetMap = context.getKeyToMemoWorksheetMap();
		for(MemoWorksheetTemplate template : TCollectionUtils.nullSafeList(context.getMemoWorksheetTemplates())){
			String key = template.getKey();
			Set<OemCellDurationType> durationTypes = template.getDurationTypes();
			if(!keyToMemoWorksheetMap.containsKey(key)){
				continue;
			}
			MemoWorksheet worksheet = keyToMemoWorksheetMap.get(key);
			List<MemoValue> memoValues = TCollectionUtils.nullSafeList(worksheet.getValues());
			MemoValue memoValue = new MemoValue();
			if(memoValues.size() >= month){
				memoValue = memoValues.get(month-1);
			}
			MemoWorksheetReportRow reportRow = new MemoWorksheetReportRow();
			reportRow.setFsPage(template.getPageNumber());
			reportRow.setFsLine(template.getLineNumber());
			reportRow.setDescription(template.getName());
			reportRow.setFieldType(worksheet.getFieldType());
			BigDecimal mtdValue = (durationTypes.contains(OemCellDurationType.MTD)? memoValue.getMtdValue(): BigDecimal.ZERO);
			reportRow.setMtdValue(mtdValue.toString());
			if(durationTypes.contains(OemCellDurationType.YTD)){
				reportRow.setYtdEnabled(true);
			}
			BigDecimal ytdValue = (durationTypes.contains(OemCellDurationType.YTD)? memoValue.getYtdValue(): BigDecimal.ZERO);
			reportRow.setYtdValue(ytdValue.toString());
			reportRow.setStatus(worksheet.getActive()? ACTIVE : INACTIVE);
			memoWorksheetReportRows.add(reportRow);
		}
		return memoWorksheetReportRows;
	}


	private void populateWorksheetDataInContext(WorksheetExcelReportContext context) {
		MemoWorksheetRequestDto requestDto = context.getReportRequestDto().getMemoWorksheetRequestDto();
		FSEntry fsEntry = fsEntryRepo.findByIdAndDealerIdWithNullCheck(requestDto.getFsId(), UserContextProvider.getCurrentDealerId());
		List<MemoWorksheetTemplate> memoWorksheetTemplates = memoWorksheetTemplateService.getMemoWorksheetTemplates(OEM.valueOf(fsEntry.getOemId()), fsEntry.getYear(), fsEntry.getVersion());
		List<MemoWorksheet> memoWorksheets = memoWorksheetService.getMemoWorksheetsForExcel(requestDto.getFsId(),requestDto.getMonth(),requestDto.isShowEmptyValues());
		Map<String, MemoWorksheet> keyToMemoWorksheetMap = TCollectionUtils.nullSafeList(memoWorksheets).stream().collect(Collectors.toMap(MemoWorksheet::getKey, memoWorksheet -> memoWorksheet));
		context.setMemoWorksheetTemplates(memoWorksheetTemplates);
		context.setKeyToMemoWorksheetMap(keyToMemoWorksheetMap);
	}

	private void populateDefaultsInContext(WorksheetExcelReportContext context) {
		MemoWorksheetExcelRequestDto reportRequestDto = JsonUtil.fromJson(JsonUtil.toJson(context.getNextBatchRequestV2().getOriginalPayload()), MemoWorksheetExcelRequestDto.class).orElse(null);
		context.setReportRequestDto(reportRequestDto);
	}

	public Set<SheetInfoDto> setSheetIdentifier(SheetDetails sheetDetail) {
		SheetInfoDto correspondentSheetInfoDto = new SheetInfoDto();
		correspondentSheetInfoDto.setSheetIdentifier(sheetDetail.getSheetIdentifier());
		correspondentSheetInfoDto.setComputedColumnConfigList(sheetDetail.getColumnConfigList());

		Set<SheetInfoDto> currHashSet = new HashSet<>();
		currHashSet.add(correspondentSheetInfoDto);
		return currHashSet;
	}

	public List<SingleRowData> getSingleRowDataList(List<MemoWorksheetReportRow> memoWorksheetReportRows, SheetInfoDto sheetInfoDto){
		List<SingleRowData> singleRowDataList = Lists.newArrayList();
		for (MemoWorksheetReportRow memoWorksheetReportRow : memoWorksheetReportRows) {
			SingleRowData singleRowData = new SingleRowData();
			List<ColumnConfig> baseColumnConfigs = new ArrayList<>();
			for(ColumnConfig columnConfig : sheetInfoDto.getComputedColumnConfigList()){
				ColumnConfig copyOfColumnConfig = helper.getCopyOfColumnConfig(columnConfig);
				if (TConstants.COUNT.equals(memoWorksheetReportRow.getFieldType()) && (MemoWorksheetColumnConfig.MTD_VALUE.getBeanKey().equals(copyOfColumnConfig.getKey())
						|| MemoWorksheetColumnConfig.YTD_VALUE.getBeanKey().equals(copyOfColumnConfig.getKey()))){
					updateColumnConfigForCountTypeRow(ExcelCellFormattingHolder.INTEGER_NUMBER, copyOfColumnConfig);
				}
				if (MemoWorksheetColumnConfig.YTD_VALUE.getBeanKey().equals(copyOfColumnConfig.getKey()) && !memoWorksheetReportRow.isYtdEnabled()){
					updateColumnConfigForCountTypeRow(ExcelCellFormattingHolder.STANDARD_STRING, copyOfColumnConfig);
					memoWorksheetReportRow.setYtdValue(TConstants.BLANK_STRING);
				}
				baseColumnConfigs.add(copyOfColumnConfig);
			}
			singleRowData.setColumnConfigs(baseColumnConfigs);
			singleRowData.setObject(memoWorksheetReportRow);
			singleRowDataList.add(singleRowData);
		}
		return singleRowDataList;
	}

	private void updateColumnConfigForCountTypeRow(ExcelCellFormattingHolder formattingHolder, ColumnConfig columnConfig){
		columnConfig.setCellType(formattingHolder.getCellType());
		columnConfig.setDataType(formattingHolder.getDataType());
		columnConfig.setHorizontalAlignment(formattingHolder.getHorizontalAlignment());
		columnConfig.setPlaceHolderIfNull(formattingHolder.getPlaceHolderIfNull());
		columnConfig.setFormattingOverride(formattingHolder.getFormatOverride());
		columnConfig.setComparatorToUse(formattingHolder.getComparatorToUse());
		columnConfig.setResolverToUse(formattingHolder.getResolverToUse());
	}

	private List<MemoWorksheetReportRow> applySearchAndFiltersOnReportRows(WorksheetExcelReportContext context, List<MemoWorksheetReportRow> memoWorksheetReportRows) {
		List<MemoWorksheetReportRow> memoWorksheetReportRowsFiltered = Lists.newArrayList();
		List<WorksheetApplicableFilter> filters = TCollectionUtils.nullSafeList(context.getReportRequestDto().getMemoWorksheetRequestDto().getApplicableFilters());
		for (WorksheetApplicableFilter filter : filters) {
			if (filter.getValues().size() > 0) {
				if (LINE_NUMBER.equalsIgnoreCase(filter.getKey())){
					memoWorksheetReportRows = memoWorksheetReportRows.stream().filter(row -> filter.getValues().contains(row.getFsLine())).collect(Collectors.toList());
				}
				else if (PAGE_NUMBER.equalsIgnoreCase(filter.getKey())) {
					memoWorksheetReportRows = memoWorksheetReportRows.stream().filter(row -> filter.getValues().contains(row.getFsPage())).collect(Collectors.toList());
				}
				else if (STATUS.toUpperCase().equalsIgnoreCase(filter.getKey())) {
					List<String> filterValues = filter.getValues().stream().filter(Objects::nonNull).map(String::toLowerCase).collect(Collectors.toList());
					memoWorksheetReportRows = memoWorksheetReportRows.stream().filter(row -> filterValues.contains(row.getStatus().toLowerCase())).collect(Collectors.toList());
				}
			}
		}
		memoWorksheetReportRows.forEach(
				row -> {
					if(ACTIVE.equalsIgnoreCase(row.getStatus())){
						row.setStatus(LocaleUtils.translateLabel(ACTIVE_KEY));
					}else if(INACTIVE.equalsIgnoreCase(row.getStatus())) {
						row.setStatus(LocaleUtils.translateLabel(INACTIVE_KEY));
					}
				}
		);
		for(MemoWorksheetReportRow worksheetReportRow : TCollectionUtils.nullSafeList(memoWorksheetReportRows)){
			if (doesMatchSearchFilter(TStringUtils.nullSafeString(context.getReportRequestDto().getMemoWorksheetRequestDto().getSearchText()),
					context.getReportRequestDto().getMemoWorksheetRequestDto().getSearchableFields(), worksheetReportRow)){
				memoWorksheetReportRowsFiltered.add(worksheetReportRow);
			}
		}
		return memoWorksheetReportRowsFiltered;
	}

	boolean doesMatchSearchFilter(String searchText, List<String> searchableFields, MemoWorksheetReportRow worksheetReportRow) {
		String description = worksheetReportRow.getDescription();
		if(TStringUtils.isBlank(searchText) ) {
			return true;
		}
		else if(TCollectionUtils.isEmpty(searchableFields)){
			return true;
		}
		return searchableFields.contains("description") && StringUtils.containsIgnoreCase(description, searchText);
	}
}


