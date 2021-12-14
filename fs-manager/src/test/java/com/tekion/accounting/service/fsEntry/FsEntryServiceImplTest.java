package com.tekion.accounting.service.fsEntry;


import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.common.GlobalService;
import com.tekion.accounting.fs.common.dpProvider.DpUtils;
import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.accounting.fs.dto.fsEntry.FSEntryUpdateDto;
import com.tekion.accounting.fs.dto.fsEntry.FsEntryCreateDto;
import com.tekion.accounting.fs.dto.mappings.FsMappingInfo;
import com.tekion.accounting.fs.dto.mappings.FsMappingInfosResponseDto;
import com.tekion.accounting.fs.enums.FSType;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.repos.OemFSMappingRepo;
import com.tekion.accounting.fs.service.accountingInfo.AccountingInfoService;
import com.tekion.accounting.fs.service.fsEntry.FsEntryServiceImpl;
import com.tekion.admin.beans.dealersetting.DealerMaster;
import com.tekion.as.client.AccountingClient;
import com.tekion.as.models.beans.GLAccount;
import com.tekion.clients.preference.client.PreferenceClient;
import com.tekion.core.beans.TResponse;
import com.tekion.core.es.request.ESResponse;
import com.tekion.core.utils.UserContext;
import com.tekion.core.utils.UserContextProvider;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DpUtils.class)
public class FsEntryServiceImplTest extends TestCase {
    @InjectMocks
    FsEntryServiceImpl fsEntryService;

    @Mock
    DealerConfig dealerConfig;
    @Mock
    FSEntryRepo fsEntryRepo;
    @Mock
    AccountingInfoService accountingInfoService;
    @Mock
    OemFSMappingRepo oemFSMappingRepo;
    @Mock
    GlobalService globalService;
    @Mock
    PreferenceClient preferenceClient;
    @Mock
    AccountingClient accountingClient;

    @Before
    public void setUp() {
        UserContextProvider.setContext(new UserContext("-1", "ca", "4"));
        Mockito.when(dealerConfig.getDealerCountryCode()).thenReturn("US");
        mockStatic(System.class);
        when(System.getenv("CLUSTER_TYPE")).thenReturn("local");
    }

    @Test
    public void testCreateFSEntry() {
        Mockito.when(fsEntryRepo.find(Mockito.anyString(), Mockito.anyInt(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(getFSEntries());
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
        Mockito.when(fsEntryRepo.fetchAllByDealerIdAndSiteId(Mockito.anyString(), Mockito.anyString())).thenReturn(getFSEntries());
        Mockito.when(accountingClient.getGLAccountList(Mockito.any())).thenReturn(getGlAccountESResponse());
        assertEquals(getFsMappingInfosResponseDto(), fsEntryService.getAllFSEntries());
    }

    @Test
    public void testGetFSEntry() {
        Mockito.when(fsEntryRepo.findByOemFsTypeDealerIdAndSiteId(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(getFSEntries());
        Mockito.when(accountingClient.getGLAccountList(Mockito.any())).thenReturn(getGlAccountESResponse());
        assertEquals(getFsMappingInfosResponseDto(), fsEntryService.getFSEntry("Volvo"));
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
        assertEquals(getFSEntry(), fsEntryService.updateFSEntry("12345", new FSEntryUpdateDto()));
    }

    @Test
    public void testDeleteFsEntryById() {
        Mockito.when(fsEntryRepo.findByIdAndDealerId(Mockito.anyString(), Mockito.anyString())).thenReturn(getFSEntry());
        Mockito.when(fsEntryRepo.findFSEntriesByOem(Mockito.anyString(), Mockito.anyString())).thenReturn(getFSEntries());
        Mockito.when(fsEntryRepo.save(Mockito.any())).thenReturn(getFSEntry());
        assertEquals(getFSEntry(), fsEntryService.deleteFsEntryById("12345"));
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
        com.tekion.clients.preference.beans.responseEntity.TResponse<List<DealerMaster>> tResponse = new com.tekion.clients.preference.beans.responseEntity.TResponse<>();
        tResponse.setData(new ArrayList<>());
        Mockito.when(preferenceClient.getAllDealerMastersWithSelectedFields(Mockito.any())).thenReturn(tResponse);
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

    private FsMappingInfosResponseDto getFsMappingInfosResponseDto() {
        FsMappingInfosResponseDto fsMappingInfosResponseDto = new FsMappingInfosResponseDto();
        FsMappingInfo fsMappingInfo1 = new FsMappingInfo();
        fsMappingInfo1.setId("6155a7d8b3cb1f0006868cd6");
        fsMappingInfo1.setYear(2021);
        fsMappingInfo1.setVersion(1);
        fsMappingInfo1.setOemId("Acura");
        fsMappingInfo1.setSiteId("-1_5");
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
        FSEntry fsEntry1 = new FSEntry();
        fsEntry1.setDealerId("5");
        fsEntry1.setYear(2021);
        fsEntry1.setVersion(1);
        fsEntry1.setOemId("Acura");
        fsEntry1.setId("6155a7d8b3cb1f0006868cd6");
        fsEntry1.setSiteId("-1_5");

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
        return fsEntry;
    }
}
