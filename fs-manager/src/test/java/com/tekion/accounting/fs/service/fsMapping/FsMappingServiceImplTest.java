package com.tekion.accounting.fs.service.fsMapping;

import com.tekion.accounting.fs.beans.common.AccountingOemFsCellGroup;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.beans.mappings.OemFsMapping;
import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.repos.OemFSMappingRepo;
import com.tekion.accounting.fs.repos.OemFsCellGroupRepo;
import com.tekion.accounting.fs.service.accountingService.AccountingService;
import com.tekion.as.models.beans.GLAccount;
import com.tekion.core.utils.UserContext;
import com.tekion.core.utils.UserContextProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;

@RunWith(MockitoJUnitRunner.class)
public class FsMappingServiceImplTest {
    @InjectMocks
    FsMappingServiceImpl fsMappingService;

    @Mock
    OemFSMappingRepo oemFSMappingRepo;

    @Mock
    DealerConfig dealerConfig;

    @Mock
    AccountingService accountingService;

    @Mock
    OemFsCellGroupRepo oemFsCellGroupRepo;

    @Mock
    FSEntryRepo fsEntryRepo;

    @Before
    public void setUp() throws ExecutionException {
        MockitoAnnotations.openMocks(FsMappingServiceImplTest.class);
        UserContextProvider.setContext(new UserContext("-1", "ca", "4"));
        Mockito.when(dealerConfig.getDealerCountryCode()).thenReturn("US");
    }

    private List<OemFsMapping> oemFsMappingList() {
        OemFsMapping oemFsMapping1 = OemFsMapping.builder().glAccountId("5_001").fsCellGroupCode("_204").fsId("617a3cf25f150300060e3b57").year(2021).version(1).oemId("GM").build();
        OemFsMapping oemFsMapping2 = OemFsMapping.builder().glAccountId("5_001").fsCellGroupCode("_204").fsId("617a3cf25f150300060e3b57").year(2021).version(1).oemId("GM").build();
        OemFsMapping oemFsMapping3 = OemFsMapping.builder().glAccountId("5_001").fsCellGroupCode("_204").fsId("617a3cf25f150300060e3b57").year(2021).version(1).oemId("GM").build();
        OemFsMapping oemFsMapping4 = OemFsMapping.builder().glAccountId("5_010").fsCellGroupCode("_304").fsId("745a3cf25f150301160e3b55").year(2021).version(1).oemId("GM").build();
        OemFsMapping oemFsMapping5 = OemFsMapping.builder().glAccountId("5_010").fsCellGroupCode("_304").fsId("745a3cf25f150301160e3b55").year(2021).version(1).oemId("GM").build();
        oemFsMapping1.setId("100001");
        oemFsMapping2.setId("100002");
        oemFsMapping3.setId("100003");
        oemFsMapping4.setId("100004");
        oemFsMapping5.setId("100005");
        List<OemFsMapping> oemFsMappingList = new ArrayList<>();
        oemFsMappingList.add(oemFsMapping1);
        oemFsMappingList.add(oemFsMapping2);
        oemFsMappingList.add(oemFsMapping3);
        oemFsMappingList.add(oemFsMapping4);
        oemFsMappingList.add(oemFsMapping5);
        return oemFsMappingList;
    }

    private List<GLAccount> glAccountList() {
        List<GLAccount> glAccountList = new ArrayList<>();
        GLAccount glAccount1 = new GLAccount();
        glAccount1.setId("5_001");
        glAccount1.setAccountName("GL1");
        glAccount1.setAccountNumber("1234");
        GLAccount glAccount2 = new GLAccount();
        glAccount2.setId("5_002");
        glAccount2.setAccountName("GL2");
        glAccount2.setAccountNumber("4321");
        glAccountList.add(glAccount1);
        glAccountList.add(glAccount2);
        return glAccountList;
    }

    @Test
    public  void testDeleteDuplicateOemFsMapping_whenThereAreDuplicates() {
        Mockito.when(oemFSMappingRepo.getFSEntriesByFsIdsAndDealerId(anyList(), any())).thenReturn(oemFsMappingList());
        Mockito.doNothing().when(oemFSMappingRepo).deleteOemFsMappingByIdAndDealerId(any(), any());
        Set<String> oemFsMappingToBeDeleted = fsMappingService.deleteDuplicateMappings(Arrays.asList("617a3cf25f150300060e3b57", "745a3cf25f150301160e3b55"));
        Assert.assertEquals(3, oemFsMappingToBeDeleted.size());
    }

    @Test
    public  void testDeleteDuplicateOemFsMapping_whenThereAreNoDuplicates() {
        Mockito.when(oemFSMappingRepo.getFSEntriesByFsIdsAndDealerId(anyList(), any())).thenReturn(null);
        Mockito.doNothing().when(oemFSMappingRepo).deleteOemFsMappingByIdAndDealerId(any(), any());
        Set<String> oemFsMappingToBeDeleted = fsMappingService.deleteDuplicateMappings(Arrays.asList("617a3cf25f150300060e3b57", "745a3cf25f150301160e3b55"));
        Assert.assertEquals(0, oemFsMappingToBeDeleted.size());
    }
    @Test
    public void testDeleteInvalidOemFsMapping() {
        List<OemFsMapping> oemFsMappingList = oemFsMappingList();
        OemFsMapping oemFsMapping = OemFsMapping.builder()
                .glAccountId("5_001")
                .fsCellGroupCode("_305")
                .fsId("745a3cf25f150301160e3b55")
                .year(2021).version(1).oemId("GM").build();
        oemFsMappingList.add(oemFsMapping);

        List<AccountingOemFsCellGroup> accountingOemFsCellGroupList = new ArrayList<>();
        AccountingOemFsCellGroup accountingOemFsCellGroup1 = AccountingOemFsCellGroup.builder().groupCode("_204").build();
        AccountingOemFsCellGroup accountingOemFsCellGroup2 = AccountingOemFsCellGroup.builder().groupCode("_304").build();
        accountingOemFsCellGroupList.add(accountingOemFsCellGroup1);
        accountingOemFsCellGroupList.add(accountingOemFsCellGroup2);

        Mockito.when(oemFSMappingRepo.findMappingsByFsId(any(),any())).thenReturn(oemFsMappingList);
        Mockito.when(accountingService.getGLAccounts(anyString())).thenReturn(glAccountList());
        Mockito.when(oemFsCellGroupRepo.findByOemId(any(), eq(2021), any())).thenReturn(accountingOemFsCellGroupList);
        Set<String> oemFsMappingToBeDeleted = fsMappingService.deleteInvalidMappings("617a3cf25f150300060e3b57");
        Assert.assertEquals(3, oemFsMappingToBeDeleted.size());
    }

    @Test
    public void testGetFsMappingsByOemIdAndGroupCodes() {
        Mockito.when(fsEntryRepo.getFsEntriesByOemIds(Mockito.any(), Mockito.anyList(), Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(Arrays.asList(getFsEntry()));
        Mockito.when(oemFSMappingRepo.findMappingsByGroupCodeAndFsIds(Mockito.anyList(), Mockito.anySet(), Mockito.anyString()))
                        .thenReturn(Arrays.asList());
        fsMappingService.getFsMappingsByOemIdAndGroupCodes(2021, Arrays.asList("_220"), Arrays.asList("GM"));
        Mockito.verify(fsEntryRepo, Mockito.times(1)).getFsEntriesByOemIds(Mockito.any(), Mockito.anyList(), Mockito.anyInt(), Mockito.anyString());
        Mockito.verify(oemFSMappingRepo, Mockito.times(1)).findMappingsByGroupCodeAndFsIds(Mockito.anyList(), Mockito.anySet(), Mockito.anyString());
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
