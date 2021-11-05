package com.tekion.accounting.fs.beans;

import com.tekion.accounting.fs.beans.accountingInfo.AccountingInfo;
import com.tekion.as.models.beans.TrialBalanceRow;
import com.tekion.as.models.dto.MonthInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class FsReportContext {

	private String fsId;
	private String oemId;
	private int oemFsYear;
	private int version;
	private int requestedYear;
	private int fromYear;
	private String siteId;
	// both months are in range of 1...12
	private int requestedMonth;

	// This is generally Calender start month or FiscalYear Start Month
	private int fromMonth;

	private boolean fsByFiscalYear;
	private Long fsTime;
	private Long tillEpoch;

	@Builder.Default
	private Map<String, TrialBalanceRow> trialBalanceRowMap = new HashMap<>();

	private MonthInfo activeMonthInfo;
	private boolean includeM13 = false;
	private boolean addM13BalInDecBalances = false;
	private OemConfig oemConfig;
	private AccountingInfo accountingInfo;
	private String mmddyyyy;
	private Set<String> glIdsRelatedToMonthlyPL;

	public com.tekion.as.models.beans.fs.FsReportContext toAccountingFSRContext(){
		com.tekion.as.models.beans.fs.FsReportContext fsrContext = new com.tekion.as.models.beans.fs.FsReportContext();
		fsrContext.setFromMonth(fromMonth);
		fsrContext.setFromYear(fromYear);
		fsrContext.setRequestedMonth(requestedMonth);
		fsrContext.setRequestedYear(requestedYear);
		fsrContext.setAddM13BalInDecBalances(addM13BalInDecBalances);
		fsrContext.setIncludeM13(includeM13);
		fsrContext.setGlIdsRelatedToMonthlyPL(glIdsRelatedToMonthlyPL);
		return fsrContext;
	}

}
