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
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.utils.UserContext;
import com.tekion.core.utils.UserContextProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;
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

    @Captor
    private ArgumentCaptor<ArrayList<OemFsMapping>> captor;

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
        fsMappingService.getFsMappingsByOemIdAndGroupCodes(2021, Arrays.asList("_220"), Arrays.asList("GM"), false);
        Mockito.verify(fsEntryRepo, Mockito.times(1)).getFsEntriesByOemIds(Mockito.any(), Mockito.anyList(), Mockito.anyInt(), Mockito.anyString());
        Mockito.verify(oemFSMappingRepo, Mockito.times(1)).findMappingsByGroupCodeAndFsIds(Mockito.anyList(), Mockito.anySet(), Mockito.anyString());
    }

    @Test
    public void testDeleteMappingsByGroupCodes() {
        Mockito.when(oemFSMappingRepo.findMappingsByGroupCodeAndFsIds(Mockito.anyList(), Mockito.anySet(), Mockito.anyString()))
                .thenReturn(mappingsToUpdate());
        fsMappingService.deleteMappingsByGroupCodes(Collections.singletonList("200"), "GM", 2021, "US");
        Mockito.verify(oemFSMappingRepo,
                Mockito.times(1)).
                findMappingsByGroupCodeAndFsIds(eq(Collections.singletonList("_200")), anySet(), eq("4"));
        Mockito.verify(oemFSMappingRepo, Mockito.times(1)).updateBulk(captor.capture());
        List<OemFsMapping> capturedArgument = captor.getValue();
        Assert.assertEquals(capturedArgument.size(), 2);
        Assert.assertTrue(capturedArgument.get(0).isDeleted());
        Assert.assertEquals(capturedArgument.get(0).getId(), mappingsToUpdate().get(0).getId());
    }

    @Test
    public void testReplaceGroupCodesInMappings() {
        Map<String, String> map = new HashMap<>();
        map.put("300", null);
        map.put("200", "300");
        Mockito.when(oemFSMappingRepo.findMappingsByGroupCodeAndFsIds(Mockito.anyList(), Mockito.anySet(), Mockito.anyString()))
                .thenReturn(mappingsToUpdate());
        fsMappingService.replaceGroupCodesInMappings(map, "GM", 2021, "US");
        Mockito.verify(oemFSMappingRepo, Mockito.times(1)).updateBulk(captor.capture());
        List<OemFsMapping> capturedArgument = captor.getValue();
        Assert.assertEquals(capturedArgument.size(), 2);
        Assert.assertEquals(capturedArgument.get(0).getFsCellGroupCode(), "_300");
        Assert.assertEquals(capturedArgument.get(0).getId(), mappingsToUpdate().get(0).getId());
    }

    @Test
    public void testCopyFsMapping_success(){
        Mockito.when(fsEntryRepo.findByIdAndDealerId(Mockito.anyString(), Mockito.anyString())).thenReturn(getFsEntry());
        Mockito.when(oemFSMappingRepo.findMappingsByFsId(Mockito.anyString(), Mockito.any())).thenReturn(mappingsToUpdate());
        fsMappingService.copyFsMappings("fromFsId", "toFsID");
        Mockito.verify(fsEntryRepo, Mockito.times(1)).findByIdAndDealerId(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(oemFSMappingRepo, Mockito.times(2)).findMappingsByFsId(Mockito.anyString(), Mockito.any());
    }

    @Test(
            expected = TBaseRuntimeException.class
    )
    public void testCopyFsMapping_targetFsIdInvalid(){
        Mockito.when(fsEntryRepo.findByIdAndDealerId(Mockito.anyString(), Mockito.anyString())).thenReturn(null);
        fsMappingService.copyFsMappings("fromFsId", "toFsID");
    }

    @Test(
            expected = TBaseRuntimeException.class
    )
    public void testCopyFsMapping_toFsIdInvalid(){
        Mockito.when(fsEntryRepo.findByIdAndDealerId(Mockito.anyString(), Mockito.anyString())).thenReturn(getFsEntry());
        Mockito.when(oemFSMappingRepo.findMappingsByFsId(Mockito.anyString(), Mockito.any())).thenReturn(null);
        fsMappingService.copyFsMappings("fromFsId", "toFsID");
    }

    List<OemFsMapping> mappingsToUpdate(){
        List<OemFsMapping> mappings = new ArrayList<>();
        OemFsMapping oemFsMapping = new OemFsMapping();
        oemFsMapping.setFsCellGroupCode("_200");
        oemFsMapping.setId("1");
        oemFsMapping.setDeleted(false);
        oemFsMapping.setOemId("Acura");
        mappings.add(oemFsMapping);

        OemFsMapping oemFsMapping1 = new OemFsMapping();
        oemFsMapping1.setFsCellGroupCode("_300");
        oemFsMapping1.setId("2");
        oemFsMapping1.setDeleted(false);
        oemFsMapping.setOemId("Acura");
        mappings.add(oemFsMapping1);
        return mappings;
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
