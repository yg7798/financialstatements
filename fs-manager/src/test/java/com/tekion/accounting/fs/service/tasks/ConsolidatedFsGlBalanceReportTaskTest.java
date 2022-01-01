package com.tekion.accounting.fs.service.tasks;

import com.tekion.accounting.fs.common.utils.DealerConfig;
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
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ConsolidatedFsGlBalanceReportTaskTest extends TestCase {

    @Mock
    DealerConfig dealerConfig;
    @Mock
    AccountingService accountingService;

    @Before
    public void setUp() {
        UserContextProvider.setContext(new UserContext("-1", "ca", "4"));
        Mockito.when(dealerConfig.getDealerCountryCode()).thenReturn("US");
        Mockito.when(accountingService.getTrialBalanceReportForMonth(Mockito.anyInt(), Mockito.anyInt(), Mockito.any(), Mockito.anySet(), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(getTrialBalance());
    }

    @Test
    public void testCallForLessYearCondition() throws Exception{
        Mockito.when(accountingService.getPostAheadMonthInfo()).thenReturn(getMonthInfo1());
        ConsolidatedFsGlBalanceReportTask consolidatedFsGlBalanceReportTask=new ConsolidatedFsGlBalanceReportTask(accountingService,2021,2,true,true,true,new HashSet<>(),"5","1","1234");
        assertEquals(getTrialBalanceRowList(),consolidatedFsGlBalanceReportTask.call());
    }

    @Test
    public void testCallForHighYearCondition() throws Exception{
        Mockito.when(accountingService.getPostAheadMonthInfo()).thenReturn(getMonthInfo2());
        ConsolidatedFsGlBalanceReportTask consolidatedFsGlBalanceReportTask=new ConsolidatedFsGlBalanceReportTask(accountingService,2021,2,true,true,true,new HashSet<>(),"5","1","1234");
        assertEquals(getTrialBalanceRowList(),consolidatedFsGlBalanceReportTask.call());
    }

    private TrialBalance getTrialBalance(){
        TrialBalance trialBalance=new TrialBalance();
        trialBalance.setAccountRows(getTrialBalanceRowList());
        return trialBalance;
    }

    private MonthInfo getMonthInfo1() {
        MonthInfo monthInfo=new MonthInfo();
        monthInfo.setMonth(2);
        monthInfo.setYear(2020);
        return monthInfo;
    }

    private MonthInfo getMonthInfo2() {
        MonthInfo monthInfo=new MonthInfo();
        monthInfo.setMonth(2);
        monthInfo.setYear(2022);
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
