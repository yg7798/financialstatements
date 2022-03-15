package com.tekion.accounting.fs.service.fsMapping;

import com.tekion.accounting.fs.beans.common.AccountingOemFsCellGroup;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.beans.mappings.OemFsMapping;
import com.tekion.accounting.fs.beans.mappings.OemFsMappingDetail;
import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.accounting.fs.dto.mappings.GroupCodeMappingDetails;
import com.tekion.accounting.fs.dto.mappings.GroupCodesVsGLAccounts;
import com.tekion.accounting.fs.dto.mappings.OemFsGroupCodeDetails;
import com.tekion.accounting.fs.dto.mappings.OemFsMappingUpdateDto;
import com.tekion.accounting.fs.events.MappingUpdateEvent;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.repos.OemFSMappingRepo;
import com.tekion.accounting.fs.repos.OemFsCellGroupRepo;
import com.tekion.accounting.fs.service.accountingService.AccountingService;
import com.tekion.accounting.fs.service.eventing.producers.FSEventHelper;
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

import static com.tekion.core.utils.UserContextProvider.getCurrentDealerId;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class FsMappingServiceImplTest {
    @InjectMocks
    FsMappingServiceImpl fsMappingService;

    @Mock
    OemFSMappingRepo oemFSMappingRepo;

    @Mock
    FSEntryRepo fsEntryRepo;

    @Mock
    DealerConfig dealerConfig;

    @Mock
    AccountingService accountingService;

    @Mock
    OemFsCellGroupRepo oemFsCellGroupRepo;
    @Mock
    FSEventHelper fsEventHelper;

    @Captor
    private ArgumentCaptor<ArrayList<OemFsMapping>> captor;

    @Before
    public void setUp() throws ExecutionException {
        MockitoAnnotations.openMocks(FsMappingServiceImplTest.class);
        UserContextProvider.setContext(new UserContext("-1", "ca", "4"));
        Mockito.when(dealerConfig.getDealerCountryCode()).thenReturn("US");
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
    public void testRestrictDuplicateOemFSMapping() {
        Mockito.when(oemFSMappingRepo.findMappingsByGroupCodeAndFsIds(any(), any(), any())).thenReturn(oemFsMappingList());
        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(anyString(), anyString())).
                thenReturn(FSEntry.builder().fsType("OEM").year(2021).oemId("GM").build());
        Mockito.when(fsEntryRepo.findByIds(anyCollection(), anyString()))
                .thenReturn(Collections.singletonList(FSEntry.builder().fsType("OEM").year(2021).oemId("GM").build()));

        fsMappingService.updateOemFsMapping(oemFsMappingUpdateDtoRequest());
        ArgumentCaptor<List<OemFsMapping>> capturingOemFsMappingList = ArgumentCaptor.forClass(List.class);
        Mockito.verify(oemFSMappingRepo).updateBulk(capturingOemFsMappingList.capture());
        List<OemFsMapping> capturedOemFsMappingList = capturingOemFsMappingList.getValue();
        Assert.assertEquals(2, capturedOemFsMappingList.size());
    }

    @Test
    public void test2RestrictDuplicateOemFSMapping() {
        List<OemFsMapping> oemFsMappingList = oemFsMappingList();
        oemFsMappingList.get(0).setFsCellGroupCode("_202");
        Mockito.when(oemFSMappingRepo.findMappingsByGroupCodeAndFsIds(any(), any(), any())).thenReturn(oemFsMappingList);
        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(anyString(), anyString())).
                thenReturn(FSEntry.builder().fsType("OEM").year(2021).oemId("GM").build());
        Mockito.when(fsEntryRepo.findByIds(anyCollection(), anyString()))
                .thenReturn(Collections.singletonList(FSEntry.builder().fsType("OEM").year(2021).oemId("GM").build()));

        fsMappingService.updateOemFsMapping(oemFsMappingUpdateDtoRequest());
        ArgumentCaptor<List<OemFsMapping>> capturingOemFsMappingList = ArgumentCaptor.forClass(List.class);
        Mockito.verify(oemFSMappingRepo).updateBulk(capturingOemFsMappingList.capture());
        List<OemFsMapping> capturedOemFsMappingList = capturingOemFsMappingList.getValue();
        Assert.assertEquals(1, capturedOemFsMappingList.size());
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
    public void testKafkaMappingUpdateEvent() {
        Mockito.when(oemFSMappingRepo.findMappingsByGroupCodeAndFsIds(any(), any(), any())).thenReturn(oemFsMappingsFromDbForKafka());
        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(anyString(), anyString())).
                thenReturn(FSEntry.builder().fsType("OEM").year(2021).oemId("GM").build());
        Mockito.when(fsEntryRepo.findByIds(anyCollection(), anyString()))
                .thenReturn(Collections.singletonList(FSEntry.builder().fsType("OEM").year(2021).oemId("GM").build()));
        Mockito.when(oemFSMappingRepo.findMappingsByFsId(anyString(), anyString())).thenReturn(updatedMappingsFroKafka());

        fsMappingService.updateOemFsMapping(oemFsMappingUpdateDtoRequestForKafka());
        ArgumentCaptor<MappingUpdateEvent> capturingOemFsMappingList = ArgumentCaptor.forClass(MappingUpdateEvent.class);
        Mockito.verify(fsEventHelper, times(2)).dispatchEventForMappingUpdate(capturingOemFsMappingList.capture());

        List<MappingUpdateEvent> mappingUpdateEvents = capturingOemFsMappingList.getAllValues();
        Assert.assertEquals(2, mappingUpdateEvents.size());
        Assert.assertEquals("gc1", mappingUpdateEvents.get(0).getGroupCode());
        Assert.assertNull(mappingUpdateEvents.get(0).getPrevGlAccounts());
        Assert.assertEquals(new HashSet<>(Arrays.asList("gl1", "gl2")), mappingUpdateEvents.get(0).getCurrentGlAccounts());

        Assert.assertEquals("gc3", mappingUpdateEvents.get(1).getGroupCode());
        Assert.assertEquals(new HashSet<>(Collections.singletonList("gl3")), mappingUpdateEvents.get(1).getPrevGlAccounts());
        Assert.assertNull(mappingUpdateEvents.get(1).getCurrentGlAccounts());
    }

    private OemFsMappingUpdateDto oemFsMappingUpdateDtoRequest() {
        OemFsMappingUpdateDto oemFsMappingUpdateDto = new OemFsMappingUpdateDto();
        oemFsMappingUpdateDto.setFsId("617a3cf25f150300060e3b57");

        OemFsMappingDetail oemFsMappingDetail1 = new OemFsMappingDetail();
        oemFsMappingDetail1.setGlAccountId("5_001");
        oemFsMappingDetail1.setFsCellGroupCode("_202");

        OemFsMappingDetail oemFsMappingDetail2 = new OemFsMappingDetail();
        oemFsMappingDetail2.setGlAccountId("5_001");
        oemFsMappingDetail2.setFsCellGroupCode("_202");

        OemFsMappingDetail oemFsMappingDetail3 = new OemFsMappingDetail();
        oemFsMappingDetail3.setGlAccountId("5_001");
        oemFsMappingDetail3.setFsCellGroupCode("_203");
        oemFsMappingUpdateDto.setMappingsToSave(Arrays.asList(oemFsMappingDetail1, oemFsMappingDetail2, oemFsMappingDetail3));
        oemFsMappingUpdateDto.setMappingsToDelete(new ArrayList<>());
        return  oemFsMappingUpdateDto;
    }


    private OemFsMappingUpdateDto oemFsMappingUpdateDtoRequestForKafka() {
        OemFsMappingUpdateDto oemFsMappingUpdateDto = new OemFsMappingUpdateDto();
        oemFsMappingUpdateDto.setFsId("1");

        OemFsMappingDetail oemFsMappingDetail1 = new OemFsMappingDetail();
        oemFsMappingDetail1.setGlAccountId("gl1");
        oemFsMappingDetail1.setFsCellGroupCode("gc1");

        OemFsMappingDetail oemFsMappingDetail2 = new OemFsMappingDetail();
        oemFsMappingDetail2.setGlAccountId("gl2");
        oemFsMappingDetail2.setFsCellGroupCode("gc1");

        OemFsMappingDetail oemFsMappingDetail3 = new OemFsMappingDetail();
        oemFsMappingDetail3.setGlAccountId("gl3");
        oemFsMappingDetail3.setFsCellGroupCode("gc3");
        oemFsMappingUpdateDto.setMappingsToSave(Arrays.asList(oemFsMappingDetail1, oemFsMappingDetail2));
        oemFsMappingUpdateDto.setMappingsToDelete(Collections.singletonList(oemFsMappingDetail3));
        return  oemFsMappingUpdateDto;
    }

    private List<OemFsMapping> oemFsMappingsFromDbForKafka() {
        OemFsMapping oemFsMapping1 = OemFsMapping.builder().glAccountId("gl3").fsCellGroupCode("gc3").build();
        List<OemFsMapping> oemFsMappingList = new ArrayList<>();
        oemFsMappingList.add(oemFsMapping1);
        return oemFsMappingList;
    }

    private List<OemFsMapping> updatedMappingsFroKafka() {
        OemFsMapping m1 = OemFsMapping.builder().glAccountId("gl1").fsCellGroupCode("gc1").build();
        OemFsMapping m2 = OemFsMapping.builder().glAccountId("gl2").fsCellGroupCode("gc1").build();
        List<OemFsMapping> oemFsMappingList = new ArrayList<>();
        return Arrays.asList(m1, m2);
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

    @Test
    public void testGetGLAccounts(){
            OemFsMapping oemFsMapping1 = OemFsMapping.builder().glAccountId("5_001").fsCellGroupCode("_204").fsId("617a3cf25f150300060e3b57").oemId("Volvo").build();
            OemFsMapping oemFsMapping2 = OemFsMapping.builder().glAccountId("5_002").fsCellGroupCode("_205").fsId("617a3cf25f150300060e3b56").oemId("VW").build();
            List<OemFsMapping> oemFsMappingList = new ArrayList<>();
            oemFsMappingList.add(oemFsMapping1);
            oemFsMappingList.add(oemFsMapping2);
        Mockito.when(oemFSMappingRepo.getMappingsByOemIds(Mockito.anyList(), Mockito.anyList())).thenReturn(oemFsMappingList);
        Assert.assertEquals(getGroupCodeMappingDetailsList(), fsMappingService.getGLAccounts(2022, getOemFsGroupCodeDetails()));
    }

    private List<GroupCodeMappingDetails> getGroupCodeMappingDetailsList() {
        GroupCodeMappingDetails groupCodeMappingDetails1 = new GroupCodeMappingDetails();
        groupCodeMappingDetails1.setOemId("Volvo");
        groupCodeMappingDetails1.setGroupCodesMapping(getGroupCodesVsGLAccounts1());

        GroupCodeMappingDetails groupCodeMappingDetails2 = new GroupCodeMappingDetails();
        groupCodeMappingDetails2.setOemId("VW");
        groupCodeMappingDetails2.setGroupCodesMapping(getGroupCodesVsGLAccounts2());

        List<GroupCodeMappingDetails> groupCodeMappingDetails = new ArrayList<>();
        groupCodeMappingDetails.add(groupCodeMappingDetails1);
        groupCodeMappingDetails.add(groupCodeMappingDetails2);
        return groupCodeMappingDetails;
    }

    private List<GroupCodesVsGLAccounts> getGroupCodesVsGLAccounts1() {
        List<GroupCodesVsGLAccounts> groupCodesVsGLAccountsList = new ArrayList<>();

        GroupCodesVsGLAccounts glAccounts1 = new GroupCodesVsGLAccounts();
        glAccounts1.setGroupCode("_204");
        List<String> glAcctIdList1 = new ArrayList<>();
        glAcctIdList1.add("5_001");
        glAccounts1.setGlAccounts(glAcctIdList1);

        groupCodesVsGLAccountsList.add(glAccounts1);
        return groupCodesVsGLAccountsList;
    }

    private List<GroupCodesVsGLAccounts> getGroupCodesVsGLAccounts2() {
        List<GroupCodesVsGLAccounts> groupCodesVsGLAccountsList = new ArrayList<>();

        GroupCodesVsGLAccounts glAccounts2 = new GroupCodesVsGLAccounts();
        glAccounts2.setGroupCode("_205");
        List<String> glAcctIdList = new ArrayList<>();
        glAcctIdList.add("5_002");
        glAccounts2.setGlAccounts(glAcctIdList);

        groupCodesVsGLAccountsList.add(glAccounts2);
        return groupCodesVsGLAccountsList;
    }

    private List<OemFsGroupCodeDetails> getOemFsGroupCodeDetails() {
        OemFsGroupCodeDetails oemFsGroupCodeDetails1 = new OemFsGroupCodeDetails();
        oemFsGroupCodeDetails1.setOemId("Volvo");
        List<String> groupCodes = new ArrayList<>();
        groupCodes.add("_204");
        oemFsGroupCodeDetails1.setGroupCodes(groupCodes);

        OemFsGroupCodeDetails oemFsGroupCodeDetails2 = new OemFsGroupCodeDetails();
        oemFsGroupCodeDetails2.setOemId("VW");
        List<String> groupCodes1 = new ArrayList<>();
        groupCodes1.add("_205");
        oemFsGroupCodeDetails2.setGroupCodes(groupCodes1);

        List<OemFsGroupCodeDetails> oemFsGroupCodeDetailsList = new ArrayList<>();
        oemFsGroupCodeDetailsList.add(oemFsGroupCodeDetails1);
        oemFsGroupCodeDetailsList.add(oemFsGroupCodeDetails2);
        return oemFsGroupCodeDetailsList;
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
}
