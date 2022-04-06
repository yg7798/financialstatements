package com.tekion.accounting.fs.service.common.excelGeneration.enums;

import com.google.common.collect.Maps;
import com.tekion.accounting.commons.utils.LocaleUtils;
import com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs.AccAbstractColumnConfig;
import com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs.financialStatment.MemoWorksheetColumnConfig;
import com.tekion.core.excelGeneration.models.model.v2.SheetDetails;
import com.tekion.core.excelGeneration.models.utils.TCollectionUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Getter
public enum ExcelReportSheet {
	// TODO: 16/03/21: Build proper framework for ordering. As of now, keeping all the orders to 0 as of now as there will be only one sheet.
	MEMO_WORKSHEET(ExcelReportType.MEMO_WORKSHEET, "memoWorksheet", "Memo Worksheet", MemoWorksheetColumnConfig.class, 0, "", "sheet.name.memo.worksheet");


	private final ExcelReportType reportType;
	private final String sheetIdentifier;
	private final String sheetName;
	private final Class<? extends AccAbstractColumnConfig> baseColumnConfigs;
	private final int sheetOrder;
	private final String columnPreferenceReportKey;
	private final String multilingualKey;

	private static Map<String, List<ExcelReportSheet>> reportTypeToReportSheets;
	public static Map<String, List<ExcelReportSheet>> getReportTypeToGroupInfoHolder(){
		return reportTypeToReportSheets;
	}


	static {
		Map<String, List<ExcelReportSheet>> mapToSet = Maps.newHashMap();
		ExcelReportSheet[] values = ExcelReportSheet.values();
		for (ExcelReportSheet value : values) {
			mapToSet.compute(value.getReportType().name(),(key,oldVal)->{
				oldVal = TCollectionUtils.nullSafeList(oldVal);
				oldVal.add(value);
				return oldVal;
			});
		}
		reportTypeToReportSheets = mapToSet;
	}

	public static SheetDetails toPaginatedSheetDetails(ExcelReportSheet sheet){
		SheetDetails sheetDetails = toSheetDetails(sheet);
		sheetDetails.setPaginatedCall(true);
		sheetDetails.setBatchSize(500);
		return sheetDetails;
	}

	public static SheetDetails toUnPaginatedSheetDetails(ExcelReportSheet sheet) {
		SheetDetails sheetDetails = toSheetDetails(sheet);
		sheetDetails.setPaginatedCall(false);
		return sheetDetails;
	}
	public static SheetDetails toSheetDetails(ExcelReportSheet sheet) {
		SheetDetails sheetDetails = new SheetDetails();
		sheetDetails.setSheetIdentifier(sheet.getSheetIdentifier());
		sheetDetails.setSheetName(LocaleUtils.translateLabel(sheet.getMultilingualKey()));
		sheetDetails.setDerivedSheet(false);
		sheetDetails.setOrder(sheet.getSheetOrder());
		return sheetDetails;
	}


}
