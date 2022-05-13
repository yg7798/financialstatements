package com.tekion.accounting.fs.service.tasks;

import com.tekion.accounting.commons.dealer.DealerConfig;
import com.tekion.accounting.fs.common.dpProvider.DpProvider;
import com.tekion.accounting.fs.common.dpProvider.DpUtils;
import com.tekion.accounting.fs.common.utils.TimeUtils;
import com.tekion.accounting.fs.dto.context.FsReportContext;
import com.tekion.accounting.fs.service.accountingService.AccountingService;
import com.tekion.as.models.beans.TrialBalance;
import com.tekion.as.models.beans.TrialBalanceRow;
import com.tekion.as.models.dto.MonthInfo;
import com.tekion.core.utils.UserContext;
import com.tekion.core.utils.UserContextProvider;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

@RunWith(MockitoJUnitRunner.Silent.class)
@PrepareForTest(DpUtils.class)
public class ConsolidatedFsGlBalanceReportInEpochTaskTest extends TestCase {
    @Mock
    DealerConfig dealerConfig;
    @Mock
    AccountingService accountingService;
    @Mock
    FsReportContext context;
    @Mock
    DpProvider dpProvider;

    @InjectMocks
    TimeUtils timeUtils;

    @Before
    public void setUp() {
        UserContextProvider.setContext(new UserContext("-1", "ca", "4"));
        Mockito.when(dealerConfig.getDealerCountryCode()).thenReturn("US");
        Mockito.when(accountingService.getCYTrialBalanceTillDayOfMonth(Mockito.anyLong(), Mockito.anySet(), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(getTrialBalance());
        Mockito.when(dpProvider.getValForDp(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn("xyz");
        DpUtils.autowireStaticElem(dpProvider);
    }

    @Test
    public void testCall() throws Exception{
        Mockito.when(dealerConfig.getDealerTimeZone()).thenReturn(TimeZone.getDefault());
        Mockito.when(context.getMmddyyyy()).thenReturn("12/24/2021");
        Mockito.when(accountingService.getActiveMonthInfo()).thenReturn(getMonthInfo());
        ConsolidatedFsGlBalanceReportInEpochTask consolidatedFsGlBalanceReportInEpochTask=new ConsolidatedFsGlBalanceReportInEpochTask(accountingService,context,"5","1","1234");
        assertEquals(getTrialBalanceRowList(),consolidatedFsGlBalanceReportInEpochTask.call());
    }


    private TrialBalance getTrialBalance(){
        TrialBalance trialBalance=new TrialBalance();
        trialBalance.setAccountRows(getTrialBalanceRowList());
        return trialBalance;
    }

    private MonthInfo getMonthInfo() {
        MonthInfo monthInfo=new MonthInfo();
        monthInfo.setMonth(2);
        monthInfo.setYear(2020);
        return monthInfo;
    }

    private List<TrialBalanceRow> getTrialBalanceRowList(){
        List<TrialBalanceRow>list=new ArrayList<>();
        TrialBalanceRow trialBalanceRow1=new TrialBalanceRow();
        trialBalanceRow1.setAccountId("1");

        TrialBalanceRow trialBalanceRow2=new TrialBalanceRow();
        trialBalanceRow2.setAccountId("2");

        list.add(trialBalanceRow1);
        list.add(trialBalanceRow2);
        return list;
    }
}
