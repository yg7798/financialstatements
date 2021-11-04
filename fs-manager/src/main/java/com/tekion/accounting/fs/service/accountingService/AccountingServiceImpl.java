package com.tekion.accounting.fs.service.accountingService;

import com.tekion.as.client.AccountingClient;
import com.tekion.as.models.beans.AccountingSettings;
import com.tekion.as.models.beans.GLAccount;
import com.tekion.as.models.beans.TrialBalance;
import com.tekion.as.models.dto.MonthInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class AccountingServiceImpl implements AccountingService {

	public final AccountingClient accountingClient;

	@Override
	public List<GLAccount> getGLAccounts(String dealerId) {
		return accountingClient.getGLAccounts().getData();
	}

	@Override
	public TrialBalance getCYTrialBalanceTillDayOfMonth(long tillEpoch, Set<String> accountTypesToDiscard, boolean excludeInActiveGlAccounts, boolean includeM13, boolean useV2, boolean addM13BalInDecBalances) {
		return  accountingClient.getCYReport(tillEpoch, excludeInActiveGlAccounts, includeM13, useV2, addM13BalInDecBalances).getData();
	}

	@Override
	public TrialBalance getTrialBalanceReportForMonthV2(int year, int month, Set<String> accountTypesToDiscard, Boolean excludeInactiveAccounts, Boolean includeM13, boolean addM13BalInDecBalances) {
		return accountingClient.getTrialBalanceReportV2(year, month, excludeInactiveAccounts, includeM13, addM13BalInDecBalances).getData();
	}

	@Override
	public MonthInfo getActiveMonthInfo() {
		MonthInfo monthInfo = accountingClient.getActiveMonthInfo().getData();
		monthInfo.setMonth(monthInfo.getMonth()-1);
		return monthInfo;
	}

	@Override
	public MonthInfo getPostAheadMonthInfo() {
		return accountingClient.getPostAheadMonthInfo().getData();
	}

	@Override
	public TrialBalance getTrialBalanceReportForMonth(int year, int month, Long tillEpoch, Set<String> accountTypesToDiscard, Boolean excludeNoActivityAccounts, boolean m13Toggle, boolean addM13DetailsInDecBalances) {
		return null;
	}

	@Override
	public AccountingSettings getAccountingSettings() {
		return null;
	}
}
