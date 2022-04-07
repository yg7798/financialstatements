package com.tekion.accounting.fs.service.common.excelGeneration.enums;

import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs.AccAbstractColumnConfig;
import com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs.financialStatment.OEMMappingColumnConfig;
import com.tekion.core.excelGeneration.models.enums.GeneratorVersion;
import lombok.Getter;

@Getter
public enum ExcelReportType {
	FINANCIAL_STATEMENT(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, GeneratorVersion.TEMPLATE, "Financial Statement", "excelReportType.financialStatement"),
	OEM_MAPPING(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, OEMMappingColumnConfig.class , "OEM Mapping", "excelReportType.oem.mapping"),
	MEMO_WORKSHEET(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, GeneratorVersion.MULTI_SHEET , "Memo Worksheet", "excelReportType.memo.worksheet");

	private final int defaultMaxReportSize;
	private final String dpPropertyName;
	private final Class<? extends AccAbstractColumnConfig> baseColumnConfigs;
	private final Class<? extends AccAbstractColumnConfig> groupByColumnConfigs;
	private final GeneratorVersion generatorVersion;
	private final String displayName;
	private final String columnPreferenceReportKey;
	private  boolean isPdfType;
	private final String displayKey;

	private ExcelReportType(int defaultMaxReportSize, String dpPropertyName, GeneratorVersion generatorVersion, String displayName, String displayKey) {
		this(defaultMaxReportSize,dpPropertyName,generatorVersion,displayName,false, displayKey);
	}
	private ExcelReportType(int defaultMaxReportSize, String dpPropertyName, GeneratorVersion generatorVersion, String displayName, boolean isPdfType1, String displayKey) {
		this.defaultMaxReportSize = defaultMaxReportSize;
		this.dpPropertyName = dpPropertyName;
		this.baseColumnConfigs = null;
		this.columnPreferenceReportKey = null;
		this.displayName = displayName;
		this.generatorVersion = generatorVersion;
		this.groupByColumnConfigs = null;
		this.isPdfType = isPdfType1;
		this.displayKey = displayKey;
	}

	private ExcelReportType(int defaultMaxReportSize, String dpPropertyName, Class<? extends AccAbstractColumnConfig> baseColumnConfigs, String displayName, String displayKey) {
		this.defaultMaxReportSize = defaultMaxReportSize;
		this.dpPropertyName = dpPropertyName;
		this.baseColumnConfigs = baseColumnConfigs;
		this.columnPreferenceReportKey = null;
		this.displayName = displayName;
		this.generatorVersion = GeneratorVersion.SINGLE_SHEET;
		this.groupByColumnConfigs = null;
		this.displayKey = displayKey;
	}

	private ExcelReportType(int defaultMaxReportSize, String dpPropertyName, Class<? extends AccAbstractColumnConfig> baseColumnConfigs, Class<? extends AccAbstractColumnConfig> groupByColumnConfigs, String displayName, String displayKey) {
		this.defaultMaxReportSize = defaultMaxReportSize;
		this.dpPropertyName = dpPropertyName;
		this.baseColumnConfigs = baseColumnConfigs;
		this.columnPreferenceReportKey = null;
		this.displayName = displayName;
		this.generatorVersion = GeneratorVersion.SINGLE_SHEET;
		this.groupByColumnConfigs = groupByColumnConfigs;
		this.displayKey = displayKey;
	}

	private ExcelReportType(int defaultMaxReportSize, String dpPropertyName, Class<? extends AccAbstractColumnConfig> baseColumnConfigs, String displayName, String columnPreferenceReportKey, String displayKey) {
		this.defaultMaxReportSize = defaultMaxReportSize;
		this.dpPropertyName = dpPropertyName;
		this.baseColumnConfigs = baseColumnConfigs;
		this.columnPreferenceReportKey = columnPreferenceReportKey;
		this.displayName = displayName;
		this.generatorVersion = GeneratorVersion.SINGLE_SHEET;
		this.groupByColumnConfigs = null;
		this.displayKey = displayKey;
	}


	private ExcelReportType(int defaultMaxReportSize, String dpPropertyName, Class<? extends AccAbstractColumnConfig> baseColumnConfigs, Class<? extends AccAbstractColumnConfig> groupByColumnConfigs, String displayName, String columnPreferenceReportKey, String displayKey) {
		this.defaultMaxReportSize = defaultMaxReportSize;
		this.dpPropertyName = dpPropertyName;
		this.baseColumnConfigs = baseColumnConfigs;
		this.columnPreferenceReportKey = columnPreferenceReportKey;
		this.generatorVersion = GeneratorVersion.SINGLE_SHEET;
		this.displayName = displayName;
		this.groupByColumnConfigs = groupByColumnConfigs;
		this.displayKey = displayKey;
	}

	private ExcelReportType(int defaultMaxReportSize, String dpPropertyName) {
		this.defaultMaxReportSize = defaultMaxReportSize;
		this.dpPropertyName = dpPropertyName;
		this.baseColumnConfigs = null;
		this.columnPreferenceReportKey = null;
		this.displayName = null;
		this.generatorVersion = null;
		this.groupByColumnConfigs = null;
		this.displayKey = null;
	}

	public boolean isITGroupByReportType(){
//		return INQUIRY_TOOL_REPORT.equals(this) || ACCOUNTING_REPORT.equals(this)
//				|| INQUIRY_TOOL_PDF_REPORT.equals(this) || ACCOUNTING_PDF_REPORT.equals(this);
		return false;
	}

	public boolean isPDFReportType() {
		//return this.isPdfType|| INQUIRY_TOOL_PDF_REPORT.equals(this) || ACCOUNTING_PDF_REPORT.equals(this) || EXPENSE_ANALYSIS_SUMMARY_PDF.equals(this) ;
		return false;
	}

}
