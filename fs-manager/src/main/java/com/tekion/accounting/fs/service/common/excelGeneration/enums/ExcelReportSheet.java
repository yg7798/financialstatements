package com.tekion.accounting.fs.service.common.excelGeneration.enums;

import com.google.common.collect.Maps;
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
//	CA_AP_AGING(ExcelReportType.CA_AP_AGING, "apAgingReport", "AP Aging Report", ApAgingColumnConfig.class, 0, ""),
//	AP_AGING_DETAILED(ExcelReportType.AP_AGING_DETAIL, "apAgingDetailedReport", "AP Aging Detailed Report", ApAgingPOInvoiceColumnConfig.class,0,""),
//	AR_AGING_REPORT(ExcelReportType.AR_AGING_REPORT, "arAgingReport", "AR Aging Report", ArAgingColumnConfig.class, 0, "arAgingReport"),
//	CA_AR_AGING_REPORT(ExcelReportType.CA_AR_AGING_REPORT, "arAgingReport", "AR Aging Summary Report", ArAgingColumnConfig.class, 0, "arAgingReport"),
//	AR_AGING_DETAILED_REPORT(ExcelReportType.AR_AGING_DETAILED_REPORT, "arAgingDetailedReport", "AR Aging Detailed Report", ArAgingDrillDownColumnConfig.class,0,""),
//	CA_AR_AGING_DETAILED_REPORT(ExcelReportType.CA_AR_AGING_DETAILED_REPORT, "CAarAgingDetailedReport", "AR Aging Detailed Report", CAArAgingDrillDownColumnConfig.class,0,""),
	MEMO_WORKSHEET(ExcelReportType.MEMO_WORKSHEET, "memoWorksheet", "Memo Worksheet", MemoWorksheetColumnConfig.class, 0, "");
//	PAYMENTS_REPORT(ExcelReportType.PAYMENTS_REPORT, "paymentsReport", "Payments Report", PaymentsReportColumnConfig.class, 0, "positivePay"),
//
//	// CORE
//
//	// Expense / OPEX
//	EXPENSE_ANALYSIS_SUMMARY(ExcelReportType.EXPENSE_ANALYSIS_SUMMARY_EXCEL,"expenseAnalysisReport","Expense Analysis Summmary", ExpenseAnalysisSummaryColumnConfig.class,0,null),
//	EXPENSE_ANALYSIS_SUMMARY_PDF(ExcelReportType.EXPENSE_ANALYSIS_SUMMARY_PDF,"expenseAnalysisReport","Expense Analysis Summmary", ExpenseAnalysisSummaryColumnConfig.class,0,null),
//	EXPENSE_ANALYSIS_SUMMARY_PDF_PREVIEW(ExcelReportType.EXPENSE_ANALYSIS_SUMMARY_PDF_PREVIEW,"expenseAnalysisReport","Expense Analysis Summmary", ExpenseAnalysisSummaryColumnConfig.class,0,null),
//
//	// schedules
//	SCHEDULE_SUMMARY_SHEET(SCHEDULE_SUMMARY, "summarySheet","Schedule Summary", ScheduleSummaryColumnConfigs.class, 0, ""),
//	SCHEDULE_DETAIL(ExcelReportType.SCHEDULE_DETAIL, "detailSheet","Schedule Detail", ScheduleSummaryColumnConfigs.class, 0, ""),
//	SCHEDULE_BALANCE_FORWARD(ExcelReportType.SCHEDULE_BALANCE_FORWARD, "balanceForwardSheet","Schedule Balance Forward", ScheduleSummaryColumnConfigs.class, 0, ""),
//	SCHEDULE_SUMMARY_WITH_DETAIL(ExcelReportType.SCHEDULE_SUMMARY_WITH_DETAIL, "summaryDetailSheet","Schedule Detail Summary", ScheduleSummaryColumnConfigs.class, 0, ""),
//
//
//	// TB
//	TRIAL_BALANCE(ExcelReportType.TRIAL_BALANCE_REPORT, "trialBalanceReport", "Trial Balance Report", com.tekion.as.excelGenearation.columnConfigs.trialBalance.TrialBalanceColumnConfig.class, 0, ""),
//	CA_TRIAL_BALANCE(ExcelReportType.CA_TRIAL_BALANCE_REPORT, "trialBalanceReport", "Trial Balance Report", com.tekion.as.excelGenearation.columnConfigs.trialBalance.TrialBalanceColumnConfig.class, 0, ""),
//
//	// cashering
//	DAILY_DEPOSIT_RECONCILIATION(ExcelReportType.DAILY_DEPOSIT_RECONCILIATION, "dailyDepositReconciliation", "Daily Deposit Reconciliation", DailyDepositReconciliationColumnConfig.class, 0, ""),
//
//	// InquiryTool/ AccountingReport
//	INQUIRY_TOOL(ExcelReportType.INQUIRY_TOOL, "InquiryToolReport", "Inquiry Tool Report", InquiryToolColumnConfig.class, 0, ""),
//	INQUIRY_TOOL_REPORT(ExcelReportType.INQUIRY_TOOL_REPORT, "InquiryToolReport", "Inquiry Tool Report", InquiryToolColumnConfig.class, 0, INQUIRY_TOOL_ASSET_TYPE),
//	ACCOUNTING_REPORT(ExcelReportType.ACCOUNTING_REPORT, "AccountingReport", "Accounting Report", InquiryToolColumnConfig.class, 0, JOURNAL_REPORT_ASSET_TYPE),
//	INQUIRY_TOOL_PDF_REPORT(ExcelReportType.INQUIRY_TOOL_PDF_REPORT, "InquiryToolReport", "Inquiry Tool Report", InquiryToolColumnConfig.class, 0, INQUIRY_TOOL_ASSET_TYPE),
//	ACCOUNTING_PDF_REPORT(ExcelReportType.ACCOUNTING_PDF_REPORT, "AccountingReport", "Accounting Report", InquiryToolColumnConfig.class, 0, JOURNAL_REPORT_ASSET_TYPE),
//
//	// BankRec
//	BANK_RECONCILIATION_BEGIN_REC(ExcelReportType.BANK_RECONCILIATION_BEGIN_REC, "bankReconciliationBeginRec", "Bank Reconciliation Begin Rec", BeginReconciliationColumnConfig.class, 0,""),
//	BANK_RECONCILIATION_HISTORY(ExcelReportType.BANK_RECONCILIATION_HISTORY, "bankReconciliationHistory", "Bank Reconciliation History", BankReconciliationHistoryColumnConfig.class, 0, ""),
//	BANK_RECONCILIATION_RECONCILED_POSTINGS(ExcelReportType.BANK_RECONCILIATION_RECONCILED_POSTINGS,"bankReconciliationReconciledPostings","Bank Reconciliation Reconciled Postings", BankRecReconciledPostingsColumnConfig.class,0,BANK_REC_RECONCILED_POSTINGS_ASSET_TYPE),
//
//
//	// AUTO POSTING
//	// GLAM
//	GLAM(ExcelReportType.GLAM, "glamExcelReport","GLAM Excel Report", null, 0, "")
//	;


	private final ExcelReportType reportType;
	private final String sheetIdentifier;
	private final String sheetName;
	private final Class<? extends AccAbstractColumnConfig> baseColumnConfigs;
	private final int sheetOrder;
	private final String columnPreferenceReportKey;

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
		sheetDetails.setSheetName(sheet.getSheetName());
		sheetDetails.setDerivedSheet(false);
		sheetDetails.setOrder(sheet.getSheetOrder());
		return sheetDetails;
	}


}
