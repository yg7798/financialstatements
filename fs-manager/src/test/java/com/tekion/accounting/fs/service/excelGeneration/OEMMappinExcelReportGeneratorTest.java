package com.tekion.accounting.fs.service.excelGeneration;

import com.tekion.accounting.commons.dealer.DealerConfig;
import com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs.ExcelColumnConfigGeneratorService;
import com.tekion.accounting.fs.service.common.excelGeneration.dto.AccExcelRequestDto;
import com.tekion.accounting.fs.service.common.excelGeneration.enums.ExcelReportType;
import com.tekion.accounting.fs.service.common.excelGeneration.helper.ExcelReportGeneratorHelper;
import com.tekion.core.excelGeneration.models.model.FetchNextBatchRequest;
import com.tekion.core.excelGeneration.models.model.v2.FetchNextBatchRequestV2;
import com.tekion.core.utils.UserContext;
import com.tekion.core.utils.UserContextProvider;
import com.tekion.core.validation.TValidator;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.Silent.class)
public class OEMMappinExcelReportGeneratorTest extends TestCase {

    @InjectMocks
    OEMMappingExcelReportGenerator reportGenerator;

    @Mock
    OEMMappingExcelHelperService helperService;

    @Mock
    ExcelReportGeneratorHelper helper;

    @Mock
    TValidator validator;

    @Mock
    DealerConfig dealerConfig;

    @Mock
    ExcelColumnConfigGeneratorService excelColumnConfigGeneratorService;

    @Before
    public void setUp() {
        UserContextProvider.setContext(new UserContext("id1", "tenant1", "dealer1"));
        Mockito.when(dealerConfig.getDealerTimeZoneName()).thenReturn("local");
    }

    @Test
    public void testCreateExcelGenerationDto() {
        Mockito.doNothing().when(validator)
                .validate(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.when(helper.createSortToBeDoneOnLambda(Mockito.anyString(), Mockito.any(), Mockito.anyBoolean()))
                .thenReturn(new ArrayList<>());
        Mockito.when(excelColumnConfigGeneratorService.columnConfigGenerator(Mockito.any()))
                        .thenReturn(new ArrayList<>());
        reportGenerator.createExcelGenerationDto(getPayload(), "OEM_MAPPING");
    }

    @Test
    public void testDoOnFetchMoreRecordsCallback() {
        Mockito.when(helperService.getExportableReportRows(Mockito.any(), Mockito.any()))
                .thenReturn(new ArrayList<>());
        reportGenerator.doOnFetchMoreRecordsCallback(getFetchNextBatchRequest(), "OEM_MAPPING");
    }

    @Test
    public void testSupportReportNames() {
        assertTrue(reportGenerator.supportedReportNames().contains(ExcelReportType.OEM_MAPPING.name()));
    }

    private FetchNextBatchRequest getFetchNextBatchRequest() {
        FetchNextBatchRequest fetchNextBatchRequest = new FetchNextBatchRequestV2();
        fetchNextBatchRequest.setAwsRequestId("awsReqId");
        fetchNextBatchRequest.setReportType("OEM_MAPPING");
        fetchNextBatchRequest.setOriginalPayload(getPayload());
        return fetchNextBatchRequest;
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
}
