package com.tekion.accounting.fs.service.fsEntry;


import com.tekion.accounting.commons.dealer.DealerConfig;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.beans.mappings.OemFsMapping;
import com.tekion.accounting.fs.common.utils.UserContextUtils;
import com.tekion.accounting.fs.dto.fsEntry.FSEntryUpdateDto;
import com.tekion.accounting.fs.dto.fsEntry.FsEntryCreateDto;
import com.tekion.accounting.fs.dto.mappings.FsMappingInfo;
import com.tekion.accounting.fs.dto.mappings.FsMappingInfosResponseDto;
import com.tekion.accounting.fs.enums.FSType;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.repos.OemFSMappingRepo;
import com.tekion.accounting.fs.service.accountingInfo.AccountingInfoService;
import com.tekion.dealersettings.client.IDealerSettingsClient;
import com.tekion.dealersettings.dealermaster.beans.DealerMaster;
import com.tekion.as.client.AccountingClient;
import com.tekion.as.models.beans.GLAccount;
import com.tekion.core.beans.TResponse;
import com.tekion.core.es.request.ESResponse;
import com.tekion.core.utils.UserContext;
import com.tekion.core.utils.UserContextProvider;
import junit.framework.TestCase;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(UserContextUtils.class)
public class FsEntryServiceImplTest extends TestCase {
    @InjectMocks
    FsEntryServiceImpl fsEntryService;

    @Mock
    DealerConfig dealerConfig;
    @Mock
    FSEntryRepo fsEntryRepo;
    @Mock
    OemFSMappingRepo oemFSMappingRepo;
    @Mock
    IDealerSettingsClient dealerSettingsClient;
    @Mock
    AccountingClient accountingClient;
    @Mock
    AccountingInfoService accountingService;

    @Captor
    ArgumentCaptor<FSEntry> fsEntryArgumentCaptor;

    @Before
    public void setUp() {
        UserContextProvider.setContext(new UserContext("-1", "ca", "4"));
        Mockito.when(dealerConfig.getDealerCountryCode()).thenReturn("US");
        PowerMockito.mockStatic(System.class);
        when(System.getenv("CLUSTER_TYPE")).thenReturn("local");
    }

    @Test
    public void testCreateFSEntry() {
        Mockito.when(fsEntryRepo.find(Mockito.anyString(), Mockito.anyInt(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(getFSEntries());
        Mockito.when(fsEntryRepo.find(Mockito.anyString(), Mockito.anyInt(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(getFSEntries1());
        Mockito.when(fsEntryRepo.save(Mockito.any())).thenReturn(getFSEntry());
        FsEntryCreateDto fsEntryCreateDto = new FsEntryCreateDto();
        List<String> dealerIds = new ArrayList<>();
        dealerIds.add("1");
        dealerIds.add("5");
        fsEntryCreateDto.setDealerIds(dealerIds);
        fsEntryCreateDto.setOemId(OEM.Acura);
        fsEntryCreateDto.setFsType(FSType.OEM);
        fsEntryCreateDto.setYear(2021);
        fsEntryCreateDto.setVersion(1);
        assertEquals(getFSEntry(), fsEntryService.createFSEntry(fsEntryCreateDto));
    }

    @Test
    public void testGetAllFSEntries() {
        Mockito.when(fsEntryRepo.getFSEntries(Mockito.anyString())).thenReturn(getFSEntries());
        Mockito.when(oemFSMappingRepo.findMappingsByFsId(Mockito.anyString(), Mockito.anyString())).thenReturn(getOemFsMappingList());
        Mockito.when(fsEntryRepo.fetchAllByDealerIdAndSiteId(Mockito.anyString(), Mockito.anyString())).thenReturn(getFSEntries());
        Mockito.when(accountingClient.getGLAccountList(Mockito.any())).thenReturn(getGlAccountESResponse());
        assertEquals(getFsMappingInfosResponseDto(), fsEntryService.getAllFSEntries());
    }

    @Test
    public void testGetFSEntry() {
        Mockito.when(fsEntryRepo.findByOemFsTypeDealerIdAndSiteId(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(getFSEntries());
        Mockito.when(accountingClient.getGLAccountList(Mockito.any())).thenReturn(getGlAccountESResponse());
        assertEquals(getFsMappingInfosResponseDto(), fsEntryService.getFSEntry("Volvo", null));
    }

    @Test
    public void testGetFSEntryById() {
        Mockito.when(fsEntryRepo.findByIdAndDealerId(Mockito.anyString(), Mockito.anyString())).thenReturn(getFSEntry());
        assertEquals(getFSEntry(), fsEntryService.getFSEntryById("1234"));
    }

    @Test
    public void testFindFsEntriesForYearBySiteId() {
        Mockito.when(fsEntryRepo.findFsEntriesForYear(Mockito.anyInt(), Mockito.anyString(), Mockito.anyString())).thenReturn(getFSEntries());
        assertEquals(getFSEntries(), fsEntryService.findFsEntriesForYear("-1_5", 2021));
    }

    @Test
    public void testUpdateFSEntry() {
        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(Mockito.anyString(), Mockito.anyString())).thenReturn(getFSEntry());
        Mockito.when(fsEntryRepo.save(Mockito.any())).thenReturn(getFSEntry());
        assertEquals(getFSEntry(), fsEntryService.updateFSEntry(new FSEntryUpdateDto("", "")));
    }

    @Test
    public void testDeleteFsEntryById1() {
        Mockito.when(fsEntryRepo.findByIdAndDealerId(Mockito.anyString(), Mockito.anyString())).thenReturn(getFSEntry());
        Mockito.when(fsEntryRepo.findFSEntriesByOem(Mockito.anyString(), Mockito.anyString())).thenReturn(getFSEntries2());
        Mockito.when(fsEntryRepo.save(Mockito.any())).thenReturn(getFSEntry());
        assertEquals(getFSEntry(), fsEntryService.deleteFsEntryById("12345"));
    }

    @Test
    public void testDeleteFsEntryById2() {
        Mockito.when(fsEntryRepo.findByIdAndDealerId(Mockito.anyString(), Mockito.anyString())).thenReturn(null);
        assertEquals(null, fsEntryService.deleteFsEntryById("12345"));
    }

    @Test
    public void testFindFsEntriesForYear() {
        Mockito.when(fsEntryRepo.findFsEntriesForDealer(Mockito.anyInt(), Mockito.anyString())).thenReturn(getFSEntries());
        assertEquals(getFSEntries(), fsEntryService.findFsEntriesForYear(2021));
    }

    @Test
    public void testGetDealersDetailForConsolidatedFS() {
        FSEntry fsEntry = getFSEntry();
        fsEntry.setFsType("CONSOLIDATED");
        List<String> dealerIds = new ArrayList<>();
        dealerIds.add("1");
        dealerIds.add("5");
        fsEntry.setDealerIds(dealerIds);
        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(Mockito.anyString(), Mockito.anyString())).thenReturn(fsEntry);
        TResponse<List<DealerMaster>> tResponse = new TResponse<>();
        tResponse.setData(new ArrayList<>());
        Mockito.when(dealerSettingsClient.getAllDealerMastersWithSelectedFields(Mockito.any())).thenReturn(tResponse);
        assertEquals(new ArrayList<>(), fsEntryService.getDealersDetailForConsolidatedFS("123"));
    }

    @Test
    public void testGetFSEntriesBySiteId() {
        List<String> siteIds = new ArrayList<>();
        siteIds.add("-1_5");
        siteIds.add("5");
        Mockito.when(fsEntryRepo.getFSEntriesBySiteId(Mockito.anyString(), Mockito.anyList())).thenReturn(getFSEntries());
        Mockito.when(accountingClient.getGLAccountList(Mockito.any())).thenReturn(getGlAccountESResponse());
        assertEquals(getFsMappingInfosResponseDto(), fsEntryService.getFSEntriesBySiteId(siteIds));
    }

    @Test
    public void testMigrateFSName(){
        List<FSEntry> fsEntries = Collections.singletonList(FSEntry.builder().oemId("GM").fsType("OEM").build());
        Mockito.when(fsEntryRepo.getFSEntries(Mockito.anyString())).
                thenReturn(fsEntries);

        doAnswer(invocation -> {
            List<FSEntry> arg0 = invocation.getArgument(0);
            assertEquals("GM-OEM", arg0.get(0).getName());
            return null;
        }).when(fsEntryRepo).bulkUpsert(Mockito.anyList());
        fsEntryService.migrateFSName();
    }

    @Test
    public void updateFSEntry(){
        FSEntry fsEntry = FSEntry.builder().oemId("GM").fsType("OEM").build();
        String name = RandomStringUtils.randomAlphanumeric(FSEntry.NAME_MAX_LENGTH+2);
        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(anyString(), anyString())).thenReturn(fsEntry);
        fsEntryService.updateFSEntry(new FSEntryUpdateDto("123",  name));
        verify(fsEntryRepo, times(1)).save(fsEntryArgumentCaptor.capture());
        assertEquals(fsEntryArgumentCaptor.getValue().getName(), name.substring(0, FSEntry.NAME_MAX_LENGTH));
    }

    @Test
    public void testGetFSEntries() {
        Mockito.when(fsEntryRepo.fetchAllByDealerIdNonDeleted(Mockito.anyString())).thenReturn(getFSEntries());
        assertEquals(getFSEntries(), fsEntryService.getFSEntries());
    }

    @Test
    public void testUpdateSiteId() {
        String siteId = "s1";
        String id = "id";
        FSEntry fsEntry  =FSEntry.builder().siteId("-1").build();
        fsEntry.setId(id);
        Mockito.when(fsEntryRepo.findByIds(anyCollection(), anyString())).thenReturn(Collections.singletonList(fsEntry));
        fsEntryService.updateSiteId("123", siteId);
        ArgumentCaptor<FSEntry> argumentCaptor = ArgumentCaptor.forClass(FSEntry.class);
        Mockito.verify(fsEntryRepo, times(1)).save(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue().getSiteId(), siteId);
        assertEquals(argumentCaptor.getValue().getId(), id);
    }

    @Test
    public void testUpdateFsTypeForFsEntry(){
        Mockito.when(fsEntryRepo.findByIdAndDealerId(Mockito.anyString(), Mockito.anyString())).thenReturn(getFSEntry());
        fsEntryService.updateFsTypeForFsEntry("1234", FSType.OEM);
        ArgumentCaptor<String> argumentCaptor1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argumentCaptor2 = ArgumentCaptor.forClass(String.class);
        verify(fsEntryRepo, times(1)).updateFsTypeForFsEntry(argumentCaptor1.capture(), argumentCaptor2.capture());
        assertEquals(argumentCaptor2.getValue(), FSType.OEM.name());
    }

    @Test
    public void testUpdateFsTypeForFsEntrySameType(){
        Mockito.when(fsEntryRepo.findByIdAndDealerId(Mockito.anyString(), Mockito.anyString())).thenReturn(getFSEntry());
        fsEntryService.updateFsTypeForFsEntry("1234", FSType.INTERNAL);
        assertEquals(Long.valueOf(0), fsEntryService.updateFsTypeForFsEntry("1234", FSType.INTERNAL));
    }

    @Test
    public void testUpdateFsTypeForFsEntryIfConsolidated(){
        FSEntry fsEntry=getFSEntry();
        fsEntry.setFsType(FSType.CONSOLIDATED.name());
        Mockito.when(fsEntryRepo.findByIdAndDealerId(Mockito.anyString(), Mockito.anyString())).thenReturn(fsEntry);
        assertEquals(Long.valueOf(0), fsEntryService.updateFsTypeForFsEntry("1234", FSType.OEM));
    }

    private FsMappingInfosResponseDto getFsMappingInfosResponseDto() {
        List<String> dealerIds = new ArrayList<>();
        dealerIds.add("1");
        dealerIds.add("5");
        FsMappingInfosResponseDto fsMappingInfosResponseDto = new FsMappingInfosResponseDto();
        FsMappingInfo fsMappingInfo1 = new FsMappingInfo();
        fsMappingInfo1.setId("6155a7d8b3cb1f0006868cd6");
        fsMappingInfo1.setYear(2021);
        fsMappingInfo1.setVersion(1);
        fsMappingInfo1.setOemId("Acura");
        fsMappingInfo1.setSiteId("-1_5");
        fsMappingInfo1.setFsType(FSType.CONSOLIDATED.name());
        fsMappingInfo1.setDealerIds(dealerIds);
        fsMappingInfo1.setUnmappedAccounts(0);
        fsMappingInfo1.setMappedAccounts(0);

        FsMappingInfo fsMappingInfo2 = new FsMappingInfo();
        fsMappingInfo2.setId("61a704a8b3cb1f0006b78e12");
        fsMappingInfo2.setYear(2022);
        fsMappingInfo2.setVersion(1);
        fsMappingInfo2.setOemId("Volvo");
        fsMappingInfo2.setSiteId("-1_5");
        fsMappingInfo2.setUnmappedAccounts(0);
        fsMappingInfo2.setMappedAccounts(0);

        List<FsMappingInfo> fsMappingInfoList = new ArrayList<>();
        fsMappingInfoList.add(fsMappingInfo1);
        fsMappingInfoList.add(fsMappingInfo2);
        fsMappingInfosResponseDto.setFsMappingInfoList(fsMappingInfoList);
        return fsMappingInfosResponseDto;
    }

    private List<FSEntry> getFSEntries() {
        List<String> dealerIds = new ArrayList<>();
        dealerIds.add("1");
        dealerIds.add("5");
        FSEntry fsEntry1 = new FSEntry();
        fsEntry1.setDealerId("5");
        fsEntry1.setDealerIds(dealerIds);
        fsEntry1.setYear(2021);
        fsEntry1.setVersion(1);
        fsEntry1.setOemId("Acura");
        fsEntry1.setId("6155a7d8b3cb1f0006868cd6");
        fsEntry1.setSiteId("-1_5");
        fsEntry1.setFsType(FSType.CONSOLIDATED.name());

        FSEntry fsEntry2 = new FSEntry();
        fsEntry2.setDealerId("5");
        fsEntry2.setYear(2022);
        fsEntry2.setVersion(1);
        fsEntry2.setOemId("Volvo");
        fsEntry2.setId("61a704a8b3cb1f0006b78e12");
        fsEntry2.setSiteId("-1_5");

        List<FSEntry> fsEntries = new ArrayList<>();
        fsEntries.add(fsEntry1);
        fsEntries.add(fsEntry2);
        return fsEntries;
    }

    private List<FSEntry> getFSEntries1() {
        List<FSEntry> fsEntries = new ArrayList<>();
        return fsEntries;
    }

    private TResponse<ESResponse<GLAccount>> getGlAccountESResponse() {
        TResponse<ESResponse<GLAccount>> esResponse = new TResponse<>();
        return esResponse;
    }

    private FSEntry getFSEntry() {
        FSEntry fsEntry = new FSEntry();
        fsEntry.setDealerId("5");
        fsEntry.setYear(2021);
        fsEntry.setVersion(1);
        fsEntry.setOemId("Acura");
        fsEntry.setId("6155a7d8b3cb1f0006868cd6");
        fsEntry.setSiteId("-1_5");
        fsEntry.setFsType(FSType.INTERNAL.name());
        return fsEntry;
    }

    private List<OemFsMapping> getOemFsMappingList() {
        OemFsMapping oemFsMapping1 = new OemFsMapping();
        oemFsMapping1.setFsId("6155a7d8b3cb1f0006868cd6");
        oemFsMapping1.setDealerId("5");
        oemFsMapping1.setYear(2021);
        oemFsMapping1.setVersion(1);
        oemFsMapping1.setGlAccountId("xyz");
        oemFsMapping1.setGlAccountDealerId("abc");

        OemFsMapping oemFsMapping2 = new OemFsMapping();
        oemFsMapping2.setFsId("6155a7d8b3cb1f0006868cd4");
        oemFsMapping2.setDealerId("5");
        oemFsMapping2.setYear(2021);
        oemFsMapping2.setVersion(1);
        oemFsMapping2.setGlAccountId("xyz");
        oemFsMapping2.setGlAccountDealerId("abc");

        List<OemFsMapping> oemFsMappingList = new ArrayList<>();
        oemFsMappingList.add(oemFsMapping1);
        oemFsMappingList.add(oemFsMapping2);
        return oemFsMappingList;
    }

    private List<FSEntry> getFSEntries2() {
        FSEntry fsEntry = new FSEntry();
        fsEntry.setDealerId("5");
        fsEntry.setYear(2021);
        fsEntry.setVersion(1);
        fsEntry.setOemId("Acura");
        fsEntry.setId("6155a7d8b3cb1f0006868cd6");
        fsEntry.setSiteId("-1_5");
        List<FSEntry> fsEntries = new ArrayList<>();
        fsEntries.add(fsEntry);
        return fsEntries;
    }
}
