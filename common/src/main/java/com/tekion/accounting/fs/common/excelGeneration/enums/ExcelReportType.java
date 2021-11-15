package com.tekion.accounting.fs.common.excelGeneration.enums;

import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.common.excelGeneration.columnConfigs.AccAbstractColumnConfig;
import com.tekion.accounting.fs.common.excelGeneration.columnConfigs.financialStatment.OEMMappingColumnConfig;
import com.tekion.core.excelGeneration.models.enums.GeneratorVersion;
import lombok.Getter;

@Getter
public enum ExcelReportType {
//	INQUIRY_TOOL(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.INQUIRY_TOOL_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolColumnConfig.class,TConstants.INQUIRY_TOOL_NOTIFICATION_DISPLAY_NAME , INQUIRY_TOOL_ASSET_TYPE),
//	CA_INQUIRY_TOOL(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.INQUIRY_TOOL_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolColumnConfig.class,TConstants.INQUIRY_TOOL_NOTIFICATION_DISPLAY_NAME , INQUIRY_TOOL_ASSET_TYPE),
//	/*
//	 * Here groupBy columns are static and detailed view columns are controlled by preference selected column
//	 * */
//	INQUIRY_TOOL_GROUP_BY_ACCOUNT_SUMMARY(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.INQUIRY_TOOL_MAX_LIMIT, null, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolGroupByAccountSummaryColumnConfig.class,TConstants.INQUIRY_TOOL_NOTIFICATION_DISPLAY_NAME,INQUIRY_TOOL_ASSET_TYPE),
//	INQUIRY_TOOL_GROUP_BY_ACCOUNT_DETAIL(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.INQUIRY_TOOL_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolColumnConfig.class, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolGroupByAccountSummaryColumnConfig.class,TConstants.INQUIRY_TOOL_NOTIFICATION_DISPLAY_NAME,INQUIRY_TOOL_ASSET_TYPE),
//	INQUIRY_TOOL_GROUP_BY_JOURNAL_SUMMARY(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.INQUIRY_TOOL_MAX_LIMIT, null, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolGroupByJournalNameSummaryColumnConfig.class,TConstants.INQUIRY_TOOL_NOTIFICATION_DISPLAY_NAME,INQUIRY_TOOL_ASSET_TYPE),
//	INQUIRY_TOOL_GROUP_BY_JOURNAL_DETAIL(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.INQUIRY_TOOL_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolColumnConfig.class, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolGroupByJournalNameSummaryColumnConfig.class,TConstants.INQUIRY_TOOL_NOTIFICATION_DISPLAY_NAME,INQUIRY_TOOL_ASSET_TYPE),
//	INQUIRY_TOOL_GROUP_BY_REFERENCE_SUMMARY(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.INQUIRY_TOOL_MAX_LIMIT, null, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolGroupByReferenceSummaryColumnConfig.class,TConstants.INQUIRY_TOOL_NOTIFICATION_DISPLAY_NAME,INQUIRY_TOOL_ASSET_TYPE),
//	INQUIRY_TOOL_GROUP_BY_REFERENCE_DETAIL(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.INQUIRY_TOOL_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolColumnConfig.class, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolGroupByReferenceSummaryColumnConfig.class,TConstants.INQUIRY_TOOL_NOTIFICATION_DISPLAY_NAME,INQUIRY_TOOL_ASSET_TYPE),
//	INQUIRY_TOOL_GROUP_BY_TRANSACTION_SUMMARY(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.INQUIRY_TOOL_MAX_LIMIT, null, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolGroupByTransactionSummaryColumnConfig.class,TConstants.INQUIRY_TOOL_NOTIFICATION_DISPLAY_NAME,INQUIRY_TOOL_ASSET_TYPE),
//	INQUIRY_TOOL_GROUP_BY_TRANSACTION_DETAIL(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.INQUIRY_TOOL_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolColumnConfig.class, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolGroupByTransactionSummaryColumnConfig.class,TConstants.INQUIRY_TOOL_NOTIFICATION_DISPLAY_NAME,INQUIRY_TOOL_ASSET_TYPE),
//	INQUIRY_TOOL_GROUP_BY_CONTROL_SUMMARY(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.INQUIRY_TOOL_MAX_LIMIT, null, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolGroupByControlSummaryColumnConfig.class,TConstants.INQUIRY_TOOL_NOTIFICATION_DISPLAY_NAME,INQUIRY_TOOL_ASSET_TYPE),
//	INQUIRY_TOOL_GROUP_BY_CONTROL_DETAIL(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.INQUIRY_TOOL_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolColumnConfig.class, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolGroupByControlSummaryColumnConfig.class,TConstants.INQUIRY_TOOL_NOTIFICATION_DISPLAY_NAME,INQUIRY_TOOL_ASSET_TYPE),
//	INQUIRY_TOOL_GROUP_BY_DEPARTMENT_SUMMARY(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.INQUIRY_TOOL_MAX_LIMIT, null, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolGroupByDepartmentSummaryColumnConfig.class,TConstants.INQUIRY_TOOL_NOTIFICATION_DISPLAY_NAME,INQUIRY_TOOL_ASSET_TYPE),
//	INQUIRY_TOOL_GROUP_BY_DEPARTMENT_DETAIL(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.INQUIRY_TOOL_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolColumnConfig.class, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolGroupByDepartmentSummaryColumnConfig.class,TConstants.INQUIRY_TOOL_NOTIFICATION_DISPLAY_NAME,INQUIRY_TOOL_ASSET_TYPE),
//	INQUIRY_TOOL_GROUP_BY_CONTROL2_SUMMARY(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.INQUIRY_TOOL_MAX_LIMIT, null, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolGroupByControl2SummaryColumnConfig.class,TConstants.INQUIRY_TOOL_NOTIFICATION_DISPLAY_NAME,INQUIRY_TOOL_ASSET_TYPE),
//	INQUIRY_TOOL_GROUP_BY_CONTROL2_DETAIL(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.INQUIRY_TOOL_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolColumnConfig.class, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolGroupByControl2SummaryColumnConfig.class,TConstants.INQUIRY_TOOL_NOTIFICATION_DISPLAY_NAME,INQUIRY_TOOL_ASSET_TYPE),
//	INQUIRY_TOOL_GROUP_BY_DESCRIPTION_SUMMARY(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.INQUIRY_TOOL_MAX_LIMIT, null, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolGroupByDescriptionSummaryColumnConfig.class,TConstants.INQUIRY_TOOL_NOTIFICATION_DISPLAY_NAME,INQUIRY_TOOL_ASSET_TYPE),
//	INQUIRY_TOOL_GROUP_BY_DESCRIPTION_DETAIL(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.INQUIRY_TOOL_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolColumnConfig.class, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolGroupByDescriptionSummaryColumnConfig.class,TConstants.INQUIRY_TOOL_NOTIFICATION_DISPLAY_NAME,INQUIRY_TOOL_ASSET_TYPE),
//	INQUIRY_TOOL_GROUP_BY_DEPARTMENT_TYPE_SUMMARY(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.INQUIRY_TOOL_MAX_LIMIT, null, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolGroupByDepartmentTypeSummaryColumnConfig.class,TConstants.INQUIRY_TOOL_NOTIFICATION_DISPLAY_NAME,INQUIRY_TOOL_ASSET_TYPE),
//	INQUIRY_TOOL_GROUP_BY_DEPARTMENT_TYPE_DETAIL(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.INQUIRY_TOOL_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolColumnConfig.class, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolGroupByDepartmentTypeSummaryColumnConfig.class,TConstants.INQUIRY_TOOL_NOTIFICATION_DISPLAY_NAME,INQUIRY_TOOL_ASSET_TYPE),
//	INQUIRY_TOOL_GROUP_BY_ACCOUNT_SUB_TYPE(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.INQUIRY_TOOL_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolColumnConfig.class,TConstants.INQUIRY_TOOL_NOTIFICATION_DISPLAY_NAME , INQUIRY_TOOL_ASSET_TYPE),
//	INQUIRY_TOOL_GROUP_BY_ACCOUNT_TYPE(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.INQUIRY_TOOL_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolColumnConfig.class,TConstants.INQUIRY_TOOL_NOTIFICATION_DISPLAY_NAME , INQUIRY_TOOL_ASSET_TYPE),
//	INQUIRY_TOOL_GROUP_BY_FINANCIAL_STATEMENT_GROUP_ID(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.INQUIRY_TOOL_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolColumnConfig.class,TConstants.INQUIRY_TOOL_NOTIFICATION_DISPLAY_NAME , INQUIRY_TOOL_ASSET_TYPE),
//	INQUIRY_TOOL_GROUP_BY_FINANCIAL_STATEMENT_SUB_GROUP_ID(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.INQUIRY_TOOL_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolColumnConfig.class,TConstants.INQUIRY_TOOL_NOTIFICATION_DISPLAY_NAME , INQUIRY_TOOL_ASSET_TYPE),
//	INQUIRY_TOOL_GROUP_BY_CONTROL2_TYPE(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.INQUIRY_TOOL_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolColumnConfig.class,TConstants.INQUIRY_TOOL_NOTIFICATION_DISPLAY_NAME , INQUIRY_TOOL_ASSET_TYPE),
//	INQUIRY_TOOL_GROUP_BY_SCHEDULED_TIME(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.INQUIRY_TOOL_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolColumnConfig.class,TConstants.INQUIRY_TOOL_NOTIFICATION_DISPLAY_NAME , INQUIRY_TOOL_ASSET_TYPE),
//	JOURNAL_REPORT(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.INQUIRY_TOOL_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.inquiryTool.InquiryToolColumnConfig.class,"Accounting Report", com.tekion.as.excelGenearation.columnConfigs.ExcelColumnConfigConstants.JOURNAL_REPORT_ASSET_TYPE),


//	CASH_RECEIPT(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.cashReceipt.CashReceiptColumnConfig.class,"Cash Receipt", "cashReceiptList"),
//	JOURNAL_REPORT_SUMMARY(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.financialReport.JournalSummaryColumnConfig.class,"Financial Report"),
//	CASH_IN_BANK_REPORT(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.financialReport.CashInBankColumnConfig.class,"Financial Report"),
//	JOURNAL_REPORT_DETAILED_SUMMARY_VIEW(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.financialReport.JournalDetailedSummaryColumnConfig.class,"Financial Report"),
//	JOURNAL_REPORT_DETAILED_DETAILED_VIEW(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.financialReport.JournalDetailedDetailedColumnConfig.class,"Financial Report"),
//	GL_REPORT_DETAILED_VIEW(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.financialReport.GLReportDetailedColumnConfig.class,"Financial Report"),
//	GL_REPORT_SUMMARY_VIEW(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.financialReport.GLReportSummaryColumnConfig.class,"Financial Report"),
//	FORM1099_VENDOR_SUMMARY(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.taxReporting.Form1099VendorSummaryColumnConfig.class,"Form 1099 Vendor Summary"),
//	AR_INVOICE(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.arInvoice.ArInvoiceColumnConfig.class,"Ar Invoices", "roAr"),
//	CA_AR_INVOICE(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.arInvoice.ArInvoiceColumnConfig.class,"Ar Invoices", "roAr"),

//	PO_INVOICE(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.poInvoice.POInvoiceColumnConfig.class,"PO Invoices", "poInvoice"),
//	CA_PO_INVOICE(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.poInvoice.POInvoiceColumnConfig.class,"PO Invoices", "poInvoice"),
//
//	AR_AGING_REPORT(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.arAging.ArAgingColumnConfig.class,"Ar Aging Summary", "arAgingReport"),
//	CA_AR_AGING_REPORT(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.arAging.ArAgingColumnConfig.class,"Centralised Ar Aging Summary", "arAgingReport"),
//	/*
//	 * Override leftAlign variable if detailed view columns are static and not controlled by preference selected column
//	 * */
//	AR_AGING_DETAILED_REPORT(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.arAging.ArAgingColumnConfig.class, com.tekion.as.excelGenearation.columnConfigs.arAging.ArAgingDrillDownColumnConfig.class,"Ar Aging Detail", "arAgingReport"),
//	CA_AR_AGING_DETAILED_REPORT(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.arAging.ArAgingColumnConfig.class, com.tekion.as.excelGenearation.columnConfigs.arAging.ca.CAArAgingDrillDownColumnConfig.class,"Centralised Ar Aging Detail", "arAgingReport"),
//
//	COA_POSTING(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.coaPosting.CoaPostingColumnConfig.class,"Coa Posting"),
//	CA_COA_POSTING(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.coaPosting.CoaPostingColumnConfig.class,"CA Coa Posting"),
//	JOURNAL_ENTRY(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.journalEntry.JournalEntryColumnConfig.class,"Journal Entry"),
//	CA_JOURNAL_ENTRY(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.journalEntry.JournalEntryColumnConfig.class,"CA Journal Entry"),
//	POSTING_TAB_COA(25000, TConstants.INQUIRY_TOOL_MAX_LIMIT),
//	VENDOR_INVOICE_EXCEL(10000, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT),
//	DEFAULT_REPORT_CONFIG(10000, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT),
//	PAYMENT_EXPORT_EXCEL(15000, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT),
//	VARIABLE_OPS_SALES_EXCEL(15000, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT),
//	GL_ACCOUNT_LIST_VIEW(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, com.tekion.as.excelGenearation.columnConfigs.glAccount.GLAccountsColumnConfig.class,"Chart Of Accounts","chartofaccounts"),


	// Multi sheet
//	SCHEDULE_SUMMARY(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, GeneratorVersion.MULTI_SHEET,"Schedule Summary"),
//	SCHEDULE_DETAIL(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, GeneratorVersion.MULTI_SHEET,"Schedule Detail"),
//	SCHEDULE_BALANCE_FORWARD(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, GeneratorVersion.MULTI_SHEET,"Schedule Balance Forward"),
	FINANCIAL_STATEMENT(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, GeneratorVersion.TEMPLATE, "Financial Statement"),
	OEM_MAPPING(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, OEMMappingColumnConfig.class , "OEM Mapping"),
//	TRIAL_BALANCE_REPORT(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, GeneratorVersion.MULTI_SHEET, "Trial Balance Report"),
//	CA_TRIAL_BALANCE_REPORT(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, GeneratorVersion.MULTI_SHEET, "Trial Balance Report"),
//	DAILY_DEPOSIT_RECONCILIATION(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, GeneratorVersion.MULTI_SHEET,"Daily Deposit Reconciliation"),
//	AP_AGING(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, GeneratorVersion.MULTI_SHEET, "AP Aging Report"),
//	AP_AGING_DETAIL(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, GeneratorVersion.MULTI_SHEET, "AP Aging Detailed Report"),
//	CA_AP_AGING(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, GeneratorVersion.MULTI_SHEET, "AP Aging Report"),
	MEMO_WORKSHEET(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, GeneratorVersion.MULTI_SHEET , "Memo Worksheet"),
//	INQUIRY_TOOL_REPORT(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, GeneratorVersion.MULTI_SHEET, "Inquiry Tool Report"),
//	ACCOUNTING_REPORT(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, GeneratorVersion.MULTI_SHEET, "Accounting Report"),
//	INQUIRY_TOOL_PDF_REPORT(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, GeneratorVersion.MULTI_SHEET, "Inquiry Tool Report"),
//	ACCOUNTING_PDF_REPORT(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, GeneratorVersion.MULTI_SHEET, "Accounting Report"),
//	BANK_RECONCILIATION_BEGIN_REC(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT,GeneratorVersion.MULTI_SHEET,"Bank Reconciliation Begin Rec"),
//	BANK_RECONCILIATION_HISTORY(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, GeneratorVersion.MULTI_SHEET,"Bank Reconciliation History"),
//	BANK_RECONCILIATION_RECONCILED_POSTINGS(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, GeneratorVersion.MULTI_SHEET,"Bank Reconciliation Reconciled Postings"),
//	SCHEDULE_SUMMARY_WITH_DETAIL(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, GeneratorVersion.MULTI_SHEET,"Schedule Summary with Detail"),
//	PAYMENTS_REPORT(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, GeneratorVersion.MULTI_SHEET,"Payments Report"),
//	EXPENSE_ANALYSIS_SUMMARY_EXCEL(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, GeneratorVersion.MULTI_SHEET,"Expense Analysis"),
//	EXPENSE_ANALYSIS_SUMMARY_PDF(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, GeneratorVersion.MULTI_SHEET,"Expense Analysis"),
//	EXPENSE_ANALYSIS_SUMMARY_PDF_PREVIEW(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, GeneratorVersion.MULTI_SHEET,"Expense Analysis",true),
//	GLAM(TConstants.DEFAULT_EXCEL_PLAIN_VIEW_LIMIT, TConstants.DEFAULT_EXCEL_EXPORT_MAX_LIMIT, GeneratorVersion.MULTI_SHEET,"GL Account Mapping")
	;

	private final int defaultMaxReportSize;
	private final String dpPropertyName;
	private final Class<? extends AccAbstractColumnConfig> baseColumnConfigs;
	private final Class<? extends AccAbstractColumnConfig> groupByColumnConfigs;
	private final GeneratorVersion generatorVersion;
	private final String displayName;
	private final String columnPreferenceReportKey;
	private  boolean isPdfType;

	private ExcelReportType(int defaultMaxReportSize, String dpPropertyName, GeneratorVersion generatorVersion, String displayName) {
		this(defaultMaxReportSize,dpPropertyName,generatorVersion,displayName,false);
	}
	private ExcelReportType(int defaultMaxReportSize, String dpPropertyName, GeneratorVersion generatorVersion, String displayName, boolean isPdfType1) {
		this.defaultMaxReportSize = defaultMaxReportSize;
		this.dpPropertyName = dpPropertyName;
		this.baseColumnConfigs = null;
		this.columnPreferenceReportKey = null;
		this.displayName = displayName;
		this.generatorVersion = generatorVersion;
		this.groupByColumnConfigs = null;
		this.isPdfType = isPdfType1;
	}

	private ExcelReportType(int defaultMaxReportSize, String dpPropertyName, Class<? extends AccAbstractColumnConfig> baseColumnConfigs, String displayName) {
		this.defaultMaxReportSize = defaultMaxReportSize;
		this.dpPropertyName = dpPropertyName;
		this.baseColumnConfigs = baseColumnConfigs;
		this.columnPreferenceReportKey = null;
		this.displayName = displayName;
		this.generatorVersion = GeneratorVersion.SINGLE_SHEET;
		this.groupByColumnConfigs = null;
	}

	private ExcelReportType(int defaultMaxReportSize, String dpPropertyName, Class<? extends AccAbstractColumnConfig> baseColumnConfigs, Class<? extends AccAbstractColumnConfig> groupByColumnConfigs, String displayName) {
		this.defaultMaxReportSize = defaultMaxReportSize;
		this.dpPropertyName = dpPropertyName;
		this.baseColumnConfigs = baseColumnConfigs;
		this.columnPreferenceReportKey = null;
		this.displayName = displayName;
		this.generatorVersion = GeneratorVersion.SINGLE_SHEET;
		this.groupByColumnConfigs = groupByColumnConfigs;
	}

	private ExcelReportType(int defaultMaxReportSize, String dpPropertyName, Class<? extends AccAbstractColumnConfig> baseColumnConfigs, String displayName, String columnPreferenceReportKey) {
		this.defaultMaxReportSize = defaultMaxReportSize;
		this.dpPropertyName = dpPropertyName;
		this.baseColumnConfigs = baseColumnConfigs;
		this.columnPreferenceReportKey = columnPreferenceReportKey;
		this.displayName = displayName;
		this.generatorVersion = GeneratorVersion.SINGLE_SHEET;
		this.groupByColumnConfigs = null;
	}


	private ExcelReportType(int defaultMaxReportSize, String dpPropertyName, Class<? extends AccAbstractColumnConfig> baseColumnConfigs, Class<? extends AccAbstractColumnConfig> groupByColumnConfigs, String displayName, String columnPreferenceReportKey) {
		this.defaultMaxReportSize = defaultMaxReportSize;
		this.dpPropertyName = dpPropertyName;
		this.baseColumnConfigs = baseColumnConfigs;
		this.columnPreferenceReportKey = columnPreferenceReportKey;
		this.generatorVersion = GeneratorVersion.SINGLE_SHEET;
		this.displayName = displayName;
		this.groupByColumnConfigs = groupByColumnConfigs;

	}

	private ExcelReportType(int defaultMaxReportSize, String dpPropertyName) {
		this.defaultMaxReportSize = defaultMaxReportSize;
		this.dpPropertyName = dpPropertyName;
		this.baseColumnConfigs = null;
		this.columnPreferenceReportKey = null;
		this.displayName = null;
		this.generatorVersion = null;
		this.groupByColumnConfigs = null;
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
