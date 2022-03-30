package com.tekion.accounting.fs.service.onboarding;

import com.tekion.accounting.fs.beans.accountingInfo.AccountingInfo;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.common.utils.DealerConfig;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

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


    @Before
    public void setUp() {
        UserContextProvider.setContext(new UserContext("-1", "ca", "4"));
    }

    @Test
    public void createSnapshotsToOnboardNewDealerTest(){
        AccountingInfo accountingInfo = getAccountingInfo();
        Mockito.when(infoRepo.findByDealerIdNonDeleted(Mockito.anyString())).thenReturn(accountingInfo);
        Mockito.when(fsEntryRepo.fetchAllByDealerIdNonDeleted(Mockito.anyString())).thenReturn(getFsEntries());
        Mockito.when(infoRepo.save(accountingInfo)).thenReturn(accountingInfo);
        Mockito.when(fsEntryRepo.getAllFSEntriesByFsType(Mockito.anyString(), Mockito.anyString())).thenReturn(getFsEntries());
        Mockito.when(accountingService.getActiveMonthInfo()).thenReturn(getActiveMonthInfo());
        Mockito.when(oemFsCellCodeSnapshotRepo.findOneSnapshotByFsIdAndMonth(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(null);
        Mockito.doNothing().when(oemFsCellCodeSnapshotRepo).saveBulkSnapshot(Mockito.anyList());
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
        return new ArrayList<>(Collections.singletonList(fsEntry));
    }

    private MonthInfo getActiveMonthInfo() {
        MonthInfo monthInfo = new MonthInfo();
        monthInfo.setMonth(11);
        monthInfo.setYear(2020);
        return monthInfo;
    }
}
