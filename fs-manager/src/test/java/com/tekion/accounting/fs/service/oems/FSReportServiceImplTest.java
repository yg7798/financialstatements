package com.tekion.accounting.fs.service.oems;

import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.accounting.fs.dto.integration.FSSubmitResponse;
import com.tekion.accounting.fs.dto.request.FinancialStatementRequestDto;
import com.tekion.accounting.fs.enums.FinancialYearType;
import com.tekion.accounting.fs.integration.ProcessFinancialStatement;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.core.excelGeneration.models.model.template.SingleCellData;
import com.tekion.core.utils.UserContext;
import com.tekion.core.utils.UserContextProvider;
import com.tekion.core.validation.TValidator;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.platform.commons.util.PackageUtils;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.event.annotation.AfterTestMethod;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class FSReportServiceImplTest extends TestCase {
    @InjectMocks
    FSReportServiceImpl fsReportService;

    @Mock
    DealerConfig dealerConfig;
    @Mock
    FSEntryRepo fsEntryRepo;
    @Mock
    TValidator tValidator;
    @Mock
    HondaBrandFinancialStatementServiceImpl hondaBrandFSService;
    @Mock
    GMFinancialStatementServiceImpl gmFinancialStatementService;
    @Mock
    ToyotaFinancialStatementServiceImpl toyotaFinancialStatementService;
    @Mock
    DefaultFSServiceImpl defaultFSService;
    @Mock
    KiaFSServiceImpl kiaFSService;
    @Mock
    FordServiceImpl fordService;
    @Mock
    MazdaFSService mazdaFSService;


    @Before
    public void setUp() {
        UserContextProvider.setContext(new UserContext("-1", "ca", "4"));
        Mockito.doNothing().when(tValidator).validate(Mockito.any(), Mockito.any());
    }

    @AfterTestMethod
    public void cleanUp() {
        Mockito.reset(fsEntryRepo);
    }
    @Test
    public void testGenerateXML() {
        Mockito.when(hondaBrandFSService.generateXML(Mockito.any())).thenReturn("xyz");
        FSEntry fsEntry = new FSEntry();
        fsEntry.setOemId("Acura");
        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(Mockito.anyString(), Mockito.anyString())).thenReturn(fsEntry);
        assertEquals("xyz", fsReportService.generateXML(getFinancialStatementRequestDto()));
    }

    @Test
    public void testSubmit() {
        Mockito.when(gmFinancialStatementService.submit(Mockito.any())).thenReturn(getFSSubmitResponse());
        FSEntry fsEntry = new FSEntry();
        fsEntry.setOemId("GM");
        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(Mockito.anyString(), Mockito.anyString())).thenReturn(fsEntry);
        assertEquals(getFSSubmitResponse(), fsReportService.submit(getFinancialStatementRequestDto()));

    }

    @Test
    public void testGetStatement() {
        Mockito.when(toyotaFinancialStatementService.getStatement(Mockito.any())).thenReturn(getProcessFinancialStatement());
        FSEntry fsEntry = new FSEntry();
        fsEntry.setOemId("Toyota");
        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(Mockito.anyString(), Mockito.anyString())).thenReturn(fsEntry);
        assertEquals(getProcessFinancialStatement(), fsReportService.getStatement(getFinancialStatementRequestDto()));
    }

    @Test
    public void testDownloadFile() {
        Mockito.doNothing().when(kiaFSService).downloadFile(Mockito.any(), Mockito.any());
        FSEntry fsEntry = new FSEntry();
        fsEntry.setOemId("Kia");
        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(Mockito.anyString(), Mockito.anyString())).thenReturn(fsEntry);
        fsReportService.downloadFile(getFinancialStatementRequestDto(), null);
        Mockito.verify(kiaFSService, Mockito.times(1)).downloadFile(getFinancialStatementRequestDto(), getHttpServletResponse());
    }

    @Test
    public void testGetCellLevelFSReportData() {
        Mockito.when(fordService.getCellLevelFSReportData(Mockito.any())).thenReturn(getSingleCellDataList());
        FSEntry fsEntry = new FSEntry();
        fsEntry.setOemId("Ford");
        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(Mockito.anyString(), Mockito.anyString())).thenReturn(fsEntry);
        assertEquals(getSingleCellDataList(), fsReportService.getCellLevelFSReportData(getFinancialStatementRequestDto()));
    }

    @Test
    public void testGenerateXMLForDefault() {
        Mockito.when(defaultFSService.generateXML(Mockito.any())).thenReturn("xyz");
        FSEntry fsEntry = new FSEntry();
        fsEntry.setOemId("FCA");
        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(Mockito.anyString(), Mockito.anyString())).thenReturn(fsEntry);
        assertEquals("xyz", fsReportService.generateXML(getFinancialStatementRequestDto()));
    }

    @Test
    public void testGenerateXMLForMazda() {
        Mockito.when(mazdaFSService.generateXML(Mockito.any())).thenReturn("xyz");
        FSEntry fsEntry = new FSEntry();
        fsEntry.setOemId("Mazda");
        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(Mockito.anyString(), Mockito.anyString())).thenReturn(fsEntry);
        assertEquals("xyz", fsReportService.generateXML(getFinancialStatementRequestDto()));
    }

    private List<SingleCellData> getSingleCellDataList() {
        SingleCellData singleCellData1 = new SingleCellData();
        singleCellData1.setAddress("abc");

        SingleCellData singleCellData2 = new SingleCellData();
        singleCellData2.setAddress("xyz");

        List<SingleCellData> list = new ArrayList<>();
        list.add(singleCellData1);
        list.add(singleCellData2);
        return list;
    }

    private ProcessFinancialStatement getProcessFinancialStatement() {
        return new ProcessFinancialStatement();
    }

    private FSSubmitResponse getFSSubmitResponse() {
        FSSubmitResponse fsSubmitResponse = new FSSubmitResponse();
        fsSubmitResponse.setStatus("true");
        return fsSubmitResponse;
    }

    private HttpServletResponse getHttpServletResponse() {
        return null;
    }

    private FinancialStatementRequestDto getFinancialStatementRequestDto() {
        FinancialStatementRequestDto financialStatementRequestDto = new FinancialStatementRequestDto();
        financialStatementRequestDto.setFsId("6155a7d8b3cb1f0006868cd6");
        financialStatementRequestDto.setFinancialYearType(FinancialYearType.FISCAL_YEAR);
        return financialStatementRequestDto;
    }

}
