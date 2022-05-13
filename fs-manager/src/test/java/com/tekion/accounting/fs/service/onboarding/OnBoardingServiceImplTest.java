package com.tekion.accounting.fs.service.onboarding;

import com.tekion.accounting.commons.dealer.DealerConfig;
import com.tekion.accounting.fs.beans.accountingInfo.AccountingInfo;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.enums.FSType;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.repos.OEMFsCellCodeSnapshotRepo;
import com.tekion.accounting.fs.repos.OemFsMappingSnapshotRepo;
import com.tekion.accounting.fs.repos.accountingInfo.AccountingInfoRepo;
import com.tekion.accounting.fs.service.accountingInfo.AccountingInfoService;
import com.tekion.accounting.fs.service.accountingService.AccountingService;
import com.tekion.accounting.fs.service.compute.FsComputeService;
import com.tekion.as.client.AccountingClient;
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
import org.springframework.core.task.AsyncTaskExecutor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RunWith(MockitoJUnitRunner.class)
public class OnBoardingServiceImplTest extends TestCase {
    @InjectMocks
    OnBoardingServiceImpl dealerOnBoardService;

    @Mock
    DealerConfig dealerConfig;
    @Mock
    FSEntryRepo fsEntryRepo;
    @Mock
    AccountingInfoRepo infoRepo;
    @Mock
    AccountingClient accountingClient;
    @Mock
    OEMFsCellCodeSnapshotRepo oemFsCellCodeSnapshotRepo;
    @Mock
    AccountingInfoService accountingInfoService;
    @Mock
    AccountingService accountingService;
    @Mock
    FsComputeService fsComputeService;
    @Mock
    OemFsMappingSnapshotRepo oemFsMappingSnapshotRepo;
    @Mock
    ExecutorService executorService;
    @Mock
    AsyncTaskExecutor asyncTaskExecutor;


    @Before
    public void setUp() {
        UserContextProvider.setContext(new UserContext("-1", "ca", "4"));
        this.executorService = Executors.newCachedThreadPool();
    }

    @Test
    public void createSnapshotsToOnboardNewDealerTest() {
        AccountingInfo accountingInfo = getAccountingInfo();
        Mockito.doNothing().when(asyncTaskExecutor).execute(Mockito.any());
        Mockito.when(fsEntryRepo.findFsEntriesForDealerAndYears(Mockito.any(), Mockito.anyList(), Mockito.anyString())).thenReturn(getFsEntries());
        Mockito.when(accountingService.getActiveMonthInfo()).thenReturn(getActiveMonthInfo());
        dealerOnBoardService.createSnapshotsToOnboardNewDealer();
    }

    private AccountingInfo getAccountingInfo() {
        AccountingInfo accountingInfo = new AccountingInfo();
        accountingInfo.setId("1");
        accountingInfo.setPrimaryOEM(OEM.GM.getOem());
        accountingInfo.setSupportedOEMs(new HashSet<String>(){{
            add("Ford");
            add("FCA");
        }});
        accountingInfo.setBsdPresent(true);
        accountingInfo.setDealerId("5");
        return accountingInfo;
    }

    private List<FSEntry> getFsEntries(){
        FSEntry fsEntry= new FSEntry();
        fsEntry.setOemId("GM");
        fsEntry.setFsType(FSType.OEM.name());
        fsEntry.setYear(2021);
        fsEntry.setDealerId("5");

        FSEntry fsEntry1= new FSEntry();
        fsEntry1.setOemId("GM");
        fsEntry1.setFsType(FSType.OEM.name());
        fsEntry1.setYear(2020);
        fsEntry1.setDealerId("5");
        List<FSEntry> fsEntries = new ArrayList<>();
        fsEntries.add(fsEntry);
        fsEntries.add(fsEntry1);
        return fsEntries;
    }

    private MonthInfo getActiveMonthInfo() {
        MonthInfo monthInfo = new MonthInfo();
        monthInfo.setMonth(11);
        monthInfo.setYear(2021);
        return monthInfo;
    }
}
