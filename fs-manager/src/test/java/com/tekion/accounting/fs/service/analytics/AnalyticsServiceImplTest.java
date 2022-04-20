package com.tekion.accounting.fs.service.analytics;

import com.tekion.accounting.fs.beans.common.OEMFsCellCodeSnapshot;
import com.tekion.accounting.fs.dto.cellcode.OEMFsCellCodeSnapshotResponseDto;
import com.tekion.accounting.fs.repos.OEMFsCellCodeSnapshotRepo;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AnalyticsServiceImplTest extends TestCase {
    @InjectMocks
    AnalyticsServiceImpl analyticsService;

    @Mock
    OEMFsCellCodeSnapshotRepo cellCodeSnapshotRepo;

    @Before
    public void setUp() {
        UserContextProvider.setContext(new UserContext("-1", "ca", "4"));
    }

    @Test
    public void testGetFSCellCodeAverageWithValidRequest(){
        Set<String> codes = new HashSet<>();
        codes.add("CD1");
        codes.add("CD2");
        when(cellCodeSnapshotRepo.getFsCellCodeByTimestamp(Mockito.anyLong(),Mockito.anyLong(),Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(getFsCellCodeResponse());
        List<OEMFsCellCodeSnapshotResponseDto> responseDtos = analyticsService.getFSCellCodeAverage(1l,100l,codes,"oem123");
        Assert.assertEquals(2,responseDtos.size());
        Assert.assertEquals(150,responseDtos.stream().filter(p -> "CD1".equalsIgnoreCase(p.getCode())).findFirst().get().getValue().intValue());
        Assert.assertEquals(100,responseDtos.stream().filter(p -> "CD2".equalsIgnoreCase(p.getCode())).findFirst().get().getValue().intValue());
    }

    private List<OEMFsCellCodeSnapshot> getFsCellCodeResponse() {
        OEMFsCellCodeSnapshot oemFsCellCodeSnapshot = new OEMFsCellCodeSnapshot();
        oemFsCellCodeSnapshot.setCode("CD1");
        oemFsCellCodeSnapshot.setValue(BigDecimal.valueOf(100l));

        OEMFsCellCodeSnapshot oemFsCellCodeSnapshot2 = new OEMFsCellCodeSnapshot();
        oemFsCellCodeSnapshot2.setCode("CD1");
        oemFsCellCodeSnapshot2.setValue(BigDecimal.valueOf(200l));

        OEMFsCellCodeSnapshot oemFsCellCodeSnapshot3 = new OEMFsCellCodeSnapshot();
        oemFsCellCodeSnapshot3.setCode("CD2");
        oemFsCellCodeSnapshot3.setValue(BigDecimal.valueOf(100l));

        List<OEMFsCellCodeSnapshot> oemFsCellCodeSnapshotList = new ArrayList<>();
        oemFsCellCodeSnapshotList.add(oemFsCellCodeSnapshot);
        oemFsCellCodeSnapshotList.add(oemFsCellCodeSnapshot2);
        oemFsCellCodeSnapshotList.add(oemFsCellCodeSnapshot3);
        return oemFsCellCodeSnapshotList;
    }

}
