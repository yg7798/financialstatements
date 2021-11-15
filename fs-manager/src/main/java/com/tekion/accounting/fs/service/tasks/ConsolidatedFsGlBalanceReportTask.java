package com.tekion.accounting.fs.service.tasks;

import com.tekion.accounting.fs.service.accountingService.AccountingService;
import com.tekion.accounting.fs.common.utils.UserContextUtils;
import com.tekion.as.models.beans.TrialBalance;
import com.tekion.as.models.beans.TrialBalanceRow;
import com.tekion.as.models.dto.MonthInfo;
import com.tekion.core.utils.UserContextProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

@Slf4j
public class ConsolidatedFsGlBalanceReportTask implements Callable<List<TrialBalanceRow>> {

	private final AccountingService accountingService;
	private final String dealerId;
	private final String tenantId;
	private final String userId;
	private final int year;
	private final int month;
	private final Boolean excludeInactiveAccounts;
	private final Boolean includeM13;
	private final boolean addM13BalInDecBalances;
	private final Set<String> accountTypesToDiscard;

	public ConsolidatedFsGlBalanceReportTask(AccountingService accountingService,
											 int year, int month, Boolean excludeInactiveAccounts,
											 Boolean includeM13, boolean addM13BalInDecBalances, Set<String> accountTypesToDiscard,
											 String dealerId, String tenantId, String userId) {
		this.accountingService = accountingService;
		this.year = year;
		this.month = month;
		this.excludeInactiveAccounts = excludeInactiveAccounts;
		this.includeM13 = includeM13;
		this.addM13BalInDecBalances = addM13BalInDecBalances;
		this.accountTypesToDiscard = accountTypesToDiscard;
		this.dealerId = dealerId;
		this.tenantId = tenantId;
		this.userId = userId;
	}

	@Override
	public List<TrialBalanceRow> call() throws Exception {
		UserContextProvider.setContext(UserContextUtils.buildUserContext(dealerId,tenantId,userId));
		TrialBalance trialBalance;
		MonthInfo postAheadMonthInfo = accountingService.getPostAheadMonthInfo();
		if( year > postAheadMonthInfo.getYear() || (year == postAheadMonthInfo.getYear() && month > postAheadMonthInfo.getMonth())) {
			trialBalance = accountingService.getTrialBalanceReportForMonth(postAheadMonthInfo.getYear(), postAheadMonthInfo.getMonth(),
					null, accountTypesToDiscard, excludeInactiveAccounts, includeM13, addM13BalInDecBalances);
		}else{
			trialBalance = accountingService.getTrialBalanceReportForMonth(year, month,
					null, accountTypesToDiscard, excludeInactiveAccounts, includeM13, addM13BalInDecBalances);

		}
		return new ArrayList<>(trialBalance.getAccountRows());
	}
}
