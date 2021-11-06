package com.tekion.accounting.fs.service.accountingService;

import com.tekion.as.models.beans.AccountingSettings;
import com.tekion.as.models.beans.GLAccount;
import com.tekion.as.models.beans.TrialBalance;
import com.tekion.as.models.dto.MonthInfo;

import java.util.List;
import java.util.Set;

public interface AccountingService {
	List<GLAccount> getGLAccounts(String dealerId);

	TrialBalance getCYTrialBalanceTillDayOfMonth(long tillEpoch, Set<String> accountTypesToDiscard,
												 boolean excludeInActiveGlAccounts, boolean includeM13,
												 boolean useV2, boolean addM13BalInDecBalances);

	TrialBalance getTrialBalanceReportForMonthV2(int year, int month, Set<String> accountTypesToDiscard,
												 Boolean excludeInactiveAccounts, Boolean includeM13,
												 boolean addM13BalInDecBalances);

	TrialBalance getTrialBalanceReportForMonth(int year, int month, Long tillEpoch, Set<String> accountTypesToDiscard,
											   Boolean excludeNoActivityAccounts, boolean m13Toggle,
											   boolean addM13DetailsInDecBalances);

	AccountingSettings getAccountingSettings();

	MonthInfo getActiveMonthInfo();

	MonthInfo getPostAheadMonthInfo();
}

