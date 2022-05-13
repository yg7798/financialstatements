package com.tekion.accounting.fs.service.excelGeneration;

import com.tekion.accounting.commons.dealer.DealerConfig;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.service.common.excelGeneration.dto.AccExcelRequestDto;
import com.tekion.accounting.fs.service.common.excelGeneration.enums.ExcelReportType;
import com.tekion.accounting.fs.service.common.template.TemplateService;
import com.tekion.accounting.fs.service.oems.FinancialStatementService;
import com.tekion.core.excelGeneration.models.model.template.ExcelTemplateRequestDto;
import com.tekion.core.excelGeneration.models.model.template.FetchTemplateDataRequest;
import com.tekion.core.excelGeneration.models.model.template.SingleCellData;
import com.tekion.core.utils.UserContext;
import com.tekion.core.utils.UserContextProvider;
import com.tekion.core.validation.TValidator;
import junit.framework.TestCase;
import org.apache.poi.ss.usermodel.CellType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RunWith(PowerMockRunner.class)
public class FinancialStatementTemplateReportGeneratorTest extends TestCase {

    @InjectMocks
    FinancialStatementTemplateReportGenerator reportGenerator;

    @Mock
    TValidator validator;

    @Mock
    FSEntryRepo fsEntryRepo;

    @Mock
    TemplateService templateService;

    @Mock
    DealerConfig dealerConfig;

    @Mock
    FinancialStatementService financialStatementService;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(System.class);
        PowerMockito.when(System.getenv("CLUSTER_TYPE")).thenReturn("local");
        MockitoAnnotations.openMocks(FinancialStatementTemplateReportGeneratorTest.class);
        UserContextProvider.setContext(new UserContext("id1", "tenant1", "dealer1"));
    }

    @Test
    public void testSupportedReportNames() {
        List<String> reportList = reportGenerator.supportedReportNames();
        assertEquals(1, reportList.size());
        assertEquals(ExcelReportType.FINANCIAL_STATEMENT.name(), reportList.get(0));
    }

    @Test
    public void testCreateExcelTemplateDto() {
        Mockito.doNothing().when(validator).validate(Mockito.any(), Mockito.any());
        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(getFsEntry());
        Mockito.when(templateService.getMediaPathForFSTemplate(Mockito.anyString(), Mockito.anyInt()))
                .thenReturn("dummyUrl");
        ExcelTemplateRequestDto excelTemplateRequestDto  = reportGenerator.createExcelTemplateRequestDto(getPayload(), ExcelReportType.FINANCIAL_STATEMENT.name());
    }

    @Test
    public void testDoOnFetchMoreRecordsCallback() {
        FetchTemplateDataRequest request = new FetchTemplateDataRequest();
        request.setAwsRequestId("aswRequestUrl");
        request.setJobId("1234-5678-9876");
        request.setReportType(ExcelReportType.FINANCIAL_STATEMENT.name());
        request.setOriginalPayload(getPayload());

        SingleCellData singleCellData = new SingleCellData();
        singleCellData.setAddress("A1");
        singleCellData.setPage("1");
        singleCellData.setValue("10");
        singleCellData.setCellType(CellType.NUMERIC);
        Mockito.when(financialStatementService.getCellLevelFSReportData(Mockito.any()))
                .thenReturn(Arrays.asList(singleCellData));
        reportGenerator.doOnFetchMoreRecordsCallback(request, "FIN_STMT");
    }


    @Test(expected = Exception.class)
    public void testDoOnFetchMoreRecordsCallback_castException() {
        FetchTemplateDataRequest request = new FetchTemplateDataRequest();
        request.setAwsRequestId("aswRequestUrl");
        request.setJobId("1234-5678-9876");
        request.setReportType(ExcelReportType.FINANCIAL_STATEMENT.name());
        request.setOriginalPayload("abc");

        reportGenerator.doOnFetchMoreRecordsCallback(request, "FIN_STMT");
    }

    private AccExcelRequestDto getPayload() {
        AccExcelRequestDto accExcelRequestDto = new AccExcelRequestDto();
        accExcelRequestDto.setReportFileName("GM_20211209");
        Map<String, String> object = new HashMap<>();
        object.put("fsId", "5f32e5e989701100085278ec");
        object.put("tillEpoch", "1638338399999");
        object.put("financialYearType", "CALENDAR_YEAR");
        object.put("addM13BalInDecBalances", "false");
        accExcelRequestDto.setRequestDetails(object);
        return accExcelRequestDto;
    }

    private FSEntry getFsEntry() {
        FSEntry fsEntry = FSEntry.builder()
                .dealerId("dealer1")
                .fsType("OEM")
                .version(1)
                .year(2021)
                .oemId("GM")
        .build();
        return fsEntry;
    }
}
