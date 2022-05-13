package com.tekion.accounting.fs.service.snapshots;

import com.tekion.accounting.commons.dealer.DealerConfig;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.enums.FSType;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.repos.OEMFsCellCodeSnapshotRepo;
import com.tekion.accounting.fs.repos.OemFsMappingSnapshotRepo;
import com.tekion.accounting.fs.repos.accountingInfo.AccountingInfoRepo;
import com.tekion.accounting.fs.service.accountingInfo.AccountingInfoService;
import com.tekion.accounting.fs.service.accountingService.AccountingService;
import com.tekion.accounting.fs.service.compute.FsComputeService;
import com.tekion.as.client.AccountingClient;
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

import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class SnapshotServiceImplTest extends TestCase {
    @InjectMocks
    SnapshotServiceImpl snapshotService;

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
    public void testCreateSnapshotsForMappingsAndCellCodes(){
        Mockito.when(fsEntryRepo.findByKeyAtDealerLevel(Mockito.anyString(), Mockito.any(), Mockito.anyString())).thenReturn(getFsEntries());
        snapshotService.createSnapshotsForMappingsAndCellCodes(2020, 2);
    }

    @Test
    public void testCreateSnapshotsForCellCodes(){
        FSEntry fsEntry = getFsEntry();
        fsEntry.setYear(2000);
        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(Mockito.anyString(), Mockito.anyString())).thenReturn(fsEntry);
        Mockito.doNothing().when(oemFsCellCodeSnapshotRepo).saveBulkSnapshot(Mockito.anyList());
        snapshotService.createSnapshotsForCellCodes("1234", 2);
    }

    @Test
    public void testCreateSnapshotsForMappings(){
        Mockito.when(oemFsMappingSnapshotRepo.findOneSnapshotByYearAndMonth(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(null);
        Mockito.doNothing().when(oemFsMappingSnapshotRepo).saveBulkSnapshot(Mockito.anyList());
        snapshotService.createSnapshotsForMappings("1234", 2);
        Mockito.doNothing().when(fsComputeService).createFsMappingSnapshot("1234", 2);
    }

    private List<FSEntry> getFsEntries(){
        FSEntry fsEntry= new FSEntry();
        fsEntry.setOemId("GM");
        fsEntry.setFsType(FSType.OEM.name());
        fsEntry.setYear(2021);
        fsEntry.setDealerId("5");
        return new ArrayList<>(Collections.singletonList(fsEntry));
    }

    private FSEntry getFsEntry()
    {
        FSEntry fsEntry=new FSEntry();
        fsEntry.setDealerId("5");
        fsEntry.setYear(2021);
        fsEntry.setVersion(1);
        fsEntry.setOemId("Acura");
        fsEntry.setId("6155a7d8b3cb1f0006868cd6");
        fsEntry.setSiteId("-1_5");
        fsEntry.setFsType("INTERNAL");
        return fsEntry;
    }

}
