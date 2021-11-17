package com.tekion.accounting.fs.service.tasks;

import com.google.common.collect.Sets;
import com.tekion.accounting.fs.dto.context.FsReportContext;
import com.tekion.accounting.fs.common.dpProvider.DpUtils;
import com.tekion.accounting.fs.service.accountingService.AccountingService;
import com.tekion.accounting.fs.common.utils.TimeUtils;
import com.tekion.accounting.fs.common.utils.UserContextUtils;
import com.tekion.as.models.beans.TrialBalanceRow;
import com.tekion.as.models.dto.MonthInfo;
import com.tekion.core.utils.UserContextProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;

@Slf4j
public class ConsolidatedFsGlBalanceReportInEpochTask implements Callable<List<TrialBalanceRow>> {

	private final AccountingService accountingService;
	private final String dealerId;
	private final String tenantId;
	private final String userId;
	private final FsReportContext context;

	public ConsolidatedFsGlBalanceReportInEpochTask(AccountingService accountingService, FsReportContext context,
													String dealerId, String tenantId, String userId) {
		this.accountingService = accountingService;
		this.context = context;
		this.dealerId = dealerId;
		this.tenantId = tenantId;
		this.userId = userId;
	}

	@Override
	public List<TrialBalanceRow> call() throws Exception {
		UserContextProvider.setContext(UserContextUtils.buildUserContext(dealerId,tenantId,userId));
		List<TrialBalanceRow> accountRows = new ArrayList<>();
		try{
			long postAheadEpoch = getPostAheadMonthInfoInEpoch(accountingService.getActiveMonthInfo());
			long dayTime = TimeUtils.getEpochFromStringWithTimeZone(context.getMmddyyyy(),TimeUtils.MM_DD_YYYY);

			long tillEpoch = TimeUtils.getDateEndTimeInTimeZone(dayTime);
//            log.info("FS TIME {} {} {} {} {}", dealerId,context.getMmddyyyy(), tillEpoch, postAheadEpoch);
			if( tillEpoch > postAheadEpoch){
				tillEpoch = postAheadEpoch;
			}
//            log.info("FS TIME {} {} ",dayTime, tillEpoch);
			accountRows = accountingService.getCYTrialBalanceTillDayOfMonth(
					tillEpoch,
					Sets.newConcurrentHashSet(),
					false,
					context.isIncludeM13(),
					DpUtils.doUseTbGeneratorV2VersionForFsInOem(),
					context.isAddM13BalInDecBalances()).getAccountRows();
		}finally{
			UserContextProvider.unsetContext();
		}

		return accountRows;
	}

	private long getPostAheadMonthInfoInEpoch(MonthInfo activeMonthInfo){
		long postAheadInEpoch;
		if(activeMonthInfo.getMonth() == Calendar.DECEMBER){
			postAheadInEpoch = TimeUtils.getMonthsEndTime(activeMonthInfo.getYear()+1, 0);
		} else{
			postAheadInEpoch = TimeUtils.getMonthsEndTime(activeMonthInfo.getYear(), activeMonthInfo.getMonth()+1);
		}
		return postAheadInEpoch;
	}

}
