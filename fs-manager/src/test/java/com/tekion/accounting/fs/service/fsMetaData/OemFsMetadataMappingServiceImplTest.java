package com.tekion.accounting.fs.service.fsMetaData;

import com.tekion.accounting.commons.dealer.DealerConfig;
import com.tekion.accounting.fs.beans.common.OemFSMetadataCellsInfo;
import com.tekion.accounting.fs.common.dpProvider.DpUtils;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.repos.OemFsMetadataCellMappingRepo;
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

@RunWith(PowerMockRunner.class)
@PrepareForTest(DpUtils.class)
public class OemFsMetadataMappingServiceImplTest extends TestCase {
    @InjectMocks
    OemFsMetadataMappingServiceImpl oemFsMetadataMappingService;

    @Mock
    DealerConfig dealerConfig;
    @Mock
    OemFsMetadataCellMappingRepo oemFsMetadataCellMappingRepo;

    @Before
    public void setUp() {
        UserContextProvider.setContext(new UserContext("-1", "ca", "4"));
        Mockito.when(dealerConfig.getDealerCountryCode()).thenReturn("US");
    }

    @Test
    public void testSaveOemFsMetadataCellMappings() {
        Mockito.doNothing().when(oemFsMetadataCellMappingRepo).insertBulk(Mockito.anyList());
        oemFsMetadataMappingService.saveOemFsMetadataCellMappings(getOemFSMetadataCellMappingCreateDto());
        Mockito.verify(oemFsMetadataCellMappingRepo,Mockito.times(1)).insertBulk(new ArrayList<>());
    }

    @Test
    public void testDeleteOemFsMetadataCellMappings() {
        Mockito.doNothing().when(oemFsMetadataCellMappingRepo).delete(Mockito.anyString(),Mockito.anyInt(),Mockito.anyString(),Mockito.anyString());
        oemFsMetadataMappingService.deleteOemFsMetadataCellMappings("Acura", 2021, "5", "US");
        Mockito.verify(oemFsMetadataCellMappingRepo,Mockito.times(1)).delete("Acura",2021,"5","US");
    }

    @Test
    public void testUpdateOemFsMetadataCellMappings() {
        Mockito.doNothing().when(oemFsMetadataCellMappingRepo).update(Mockito.any());
        oemFsMetadataMappingService.updateOemFsMetadataCellMappings(getOemFsMetadataCellMappingInfo());
        Mockito.verify(oemFsMetadataCellMappingRepo,Mockito.times(1)).update(getOemFSMetadataCellsInfo());
    }

    @Test
    public void testGetOemFsMetadataCellMappings() {
        Mockito.when(oemFsMetadataCellMappingRepo.getOemFsMetadataCellMapping(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString())).thenReturn(getOemFSMetadataCellsInfo());
        assertEquals(getOemFSMetadataCellsInfo(), oemFsMetadataMappingService.getOemFsMetadataCellMappings("Acura", 2021, "1"));
    }

    private OemFSMetadataCellsInfo getOemFSMetadataCellsInfo() {
        OemFSMetadataCellsInfo oemFSMetadataCellsInfo = new OemFSMetadataCellsInfo();
        oemFSMetadataCellsInfo.setOemId("Acura");
        oemFSMetadataCellsInfo.setVersion("5");
        oemFSMetadataCellsInfo.setYear(2021);
        oemFSMetadataCellsInfo.setCountry("US");
        oemFSMetadataCellsInfo.setCellMapping(new ArrayList<>());
        return oemFSMetadataCellsInfo;
    }

    private OemFsMetadataCellMappingInfo getOemFsMetadataCellMappingInfo() {
        OemFsMetadataCellMappingInfo oemFsMetadataCellMappingInfo = new OemFsMetadataCellMappingInfo();
        oemFsMetadataCellMappingInfo.setMappings(new ArrayList<>());
        oemFsMetadataCellMappingInfo.setOem(OEM.Acura);
        oemFsMetadataCellMappingInfo.setVersion("5");
        oemFsMetadataCellMappingInfo.setYear(2021);
        oemFsMetadataCellMappingInfo.setCountry("US");
        return oemFsMetadataCellMappingInfo;
    }

    private OemFSMetadataCellMappingCreateDto getOemFSMetadataCellMappingCreateDto() {
        OemFSMetadataCellMappingCreateDto oemFSMetadataCellMappingCreateDto = new OemFSMetadataCellMappingCreateDto();
        oemFSMetadataCellMappingCreateDto.setOemFsMetadataCellMappingInfos(new ArrayList<>());
        return oemFSMetadataCellMappingCreateDto;
    }
}
