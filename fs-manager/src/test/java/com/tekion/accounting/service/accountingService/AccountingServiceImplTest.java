package com.tekion.accounting.service.accountingService;

import com.tekion.accounting.fs.common.dpProvider.DpUtils;
import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.accounting.fs.service.accountingService.AccountingServiceImpl;
import com.tekion.as.client.AccountingClient;
import com.tekion.as.models.beans.AccountingSettings;
import com.tekion.as.models.beans.GLAccount;
import com.tekion.as.models.beans.TrialBalance;
import com.tekion.as.models.beans.TrialBalanceRow;
import com.tekion.as.models.beans.fs.FsReportDto;
import com.tekion.as.models.dto.MonthInfo;
import com.tekion.core.beans.TResponse;
import com.tekion.core.es.common.impl.TekSearchRequest;
import com.tekion.core.es.request.ESResponse;
import com.tekion.core.utils.UserContext;
import com.tekion.core.utils.UserContextProvider;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anySet;

@RunWith(MockitoJUnitRunner.Silent.class)
@PrepareForTest(DpUtils.class)
public class AccountingServiceImplTest extends TestCase {

    @InjectMocks
    AccountingServiceImpl accountingService;
    @Mock
    DealerConfig dealerConfig;
    @Mock
    AccountingClient accountingClient;


    @Before
    public void setUp() {
        UserContextProvider.setContext(new UserContext("-1", "ca", "4"));
        Mockito.when(dealerConfig.getDealerTimeZone()).thenReturn(TimeZone.getTimeZone("America/Los_Angeles"));
        Mockito.when(dealerConfig.getDealerCountryCode()).thenReturn("US");
    }

    @Test
    public void testGetGLAccounts() {
        TResponse<List<GLAccount>> response = new TResponse<>();
        response.setData(getGLAccounts());
        Mockito.when(accountingClient.getGLAccounts()).thenReturn(response);
        List<GLAccount> glAccounts = accountingService.getGLAccounts("5");
        Assert.assertEquals(3, glAccounts.size());
    }

    @Test
    public void testGetCYTrialBalanceTillDayOfMonth() {
        TResponse<TrialBalance> response = new TResponse<>();
        response.setData(getTrialBalance());
        Mockito.when(accountingClient.getCYReport(Mockito.anyLong(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(response);
        TrialBalance trialBalance = accountingService.getCYTrialBalanceTillDayOfMonth(1599202256l, new HashSet<>(), true, true, true, true);
        Assert.assertNotNull(trialBalance);
    }

    @Test
    public void testGetTrialBalanceReportForMonthV2() {
        TResponse<TrialBalance> response = new TResponse<>();
        response.setData(getTrialBalance());
        Mockito.when(accountingClient.getTrialBalanceReportV2(Mockito.anyInt(), Mockito.anyInt(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(response);
        TrialBalance trialBalance =accountingService.getTrialBalanceReportForMonthV2(2021, 12, new HashSet<>(), true, true,true);
        Assert.assertNotNull(trialBalance);
    }

    @Test
    public void testGetTrialBalanceReportForMonth() {
        TrialBalance trialBalance = accountingService.getTrialBalanceReportForMonth(2021, 12, 1599202256l, new HashSet<>(), true, true, true);
        Assert.assertNull(trialBalance);
    }

    @Test
    public void testGetAccountingSettings() {
        Mockito.when(accountingClient.getAccountingSettings()).thenReturn(getAccountingSettings());
        AccountingSettings accountingSettings =accountingService.getAccountingSettings();
        Assert.assertNotNull(accountingSettings);
    }

    @Test
    public void testGetActiveMonthInfo() {
        Mockito.when(accountingClient.getActiveMonthInfo()).thenReturn(getActiveMonthInfo());
        MonthInfo monthInfo =accountingService.getActiveMonthInfo();
        Assert.assertNotNull(monthInfo);
    }

    @Test
    public void testGetPostAheadMonthInfo() {
        Mockito.when(accountingClient.getPostAheadMonthInfo()).thenReturn(getActiveMonthInfo());
        MonthInfo monthInfo = accountingService.getPostAheadMonthInfo();
        Assert.assertNotNull(monthInfo);
    }

    @Test
    public void testGetConsolidatedGlBalancesForMonth() {
        Mockito.when(accountingClient.getConsolidatedGlBalancesForMonth(Mockito.anyInt(), Mockito.anyInt(), anySet(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(getTrialBalanceList());
        List<TrialBalanceRow> trialBalanceRowList = accountingService.getConsolidatedGlBalancesForMonth(2021, 12, new HashSet<>(), true, true, true);
        Assert.assertEquals(0,trialBalanceRowList.size());
    }

    @Test
    public void testDefaultSearch() {
        ESResponse<GLAccount> esResponse = new ESResponse<>();
        esResponse.setHits(getGLAccounts());
        esResponse.setCount(esResponse.getHits().size());
        TResponse<ESResponse<GLAccount>> response = new TResponse<>();
        response.setData(esResponse);
        Mockito.when(accountingClient.getGLAccountList(Mockito.any())).thenReturn(response);
        ESResponse<GLAccount> response1 = accountingService.defaultSearch(new TekSearchRequest());
        Assert.assertNotNull(response1);
    }


    @Test
    public void testGetFSTrialBalanceTillDayOfMonth() {
        TResponse<TrialBalance> response = new TResponse<>();
        response.setData(getTrialBalance());
        Mockito.when(accountingClient.getFSTrialBalanceTillDayOfMonth(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyLong()))
                .thenReturn(response);
        TrialBalance trialBalance = accountingService.getFSTrialBalanceTillDayOfMonth(2,2021, 2, 2022, 213721362);
        Assert.assertNotNull(trialBalance);
    }

    @Test
    public void testGetGlBalCntInfoForFS(){
        FsReportDto fsReportDto = new FsReportDto();
        Mockito.when(accountingClient.getGlBalCntInfoForFS(fsReportDto)).thenReturn(getData());
        Map<Integer, Map<String, Map<String, BigDecimal>>> data = accountingService.getGlBalCntInfoForFS( new FsReportDto());
        Assert.assertNotNull(data);
    }

    private List<GLAccount> getGLAccounts() {
        GLAccount glAccount1 = new GLAccount();
        glAccount1.setId("g1");
        glAccount1.setBalance(new BigDecimal(100));
        glAccount1.setCount(5);
        glAccount1.setAccountTypeId("ASSET");
        GLAccount glAccount2 = new GLAccount();
        glAccount2.setId("g2");
        glAccount2.setBalance(new BigDecimal(-100));
        glAccount2.setCount(3);
        glAccount2.setAccountTypeId("LIABILITY");
        GLAccount glAccount3 = new GLAccount();
        glAccount3.setId("g3");
        glAccount3.setBalance(new BigDecimal(100));
        glAccount3.setCount(3);
        glAccount3.setAccountTypeId("OPERATING_EXPENSE");
        return Arrays.asList(glAccount1, glAccount2, glAccount3);
    }

    private TrialBalance getTrialBalance() {
        List<TrialBalanceRow> trialBalanceRowList = new ArrayList<>();
        trialBalanceRowList.add(getTrialBalanceRow("5_123"));
        TrialBalance trialBalance = new TrialBalance();
        trialBalance.setAccountRows(trialBalanceRowList);
        return trialBalance;
    }

    private TResponse<MonthInfo> getActiveMonthInfo() {
        MonthInfo monthInfo = new MonthInfo();
        monthInfo.setMonth(11);
        monthInfo.setYear(2020);
        TResponse<MonthInfo> response = new TResponse<>();
        response.setData(monthInfo);
        return response;
    }

    private TResponse<AccountingSettings> getAccountingSettings() {
        AccountingSettings accountingSettings = new AccountingSettings();
        accountingSettings.setFiscalYearStartMonth(1);
        TResponse<AccountingSettings> response = new TResponse<>();
        response.setData(accountingSettings);
        return response;
    }

    private TResponse<List<TrialBalanceRow>> getTrialBalanceList(){
        List<TrialBalanceRow> trialBalances = new ArrayList<>();
        TResponse<List<TrialBalanceRow>> response = new TResponse<>();
        response.setData(trialBalances);
        return response;
    }

    private TrialBalanceRow getTrialBalanceRow(String accountId){
        TrialBalanceRow trialBalanceRow = new TrialBalanceRow();
        trialBalanceRow.setAccountId(accountId);
        return trialBalanceRow;
    }

    private TResponse<Map<Integer, Map<String, Map<String, BigDecimal>>>> getData(){
        Map<Integer, Map<String, Map<String, BigDecimal>>> mapData = new HashMap<>();
        TResponse<Map<Integer, Map<String, Map<String, BigDecimal>>>> response = new TResponse<>();
        response.setData(mapData);
        return response;
    }
}