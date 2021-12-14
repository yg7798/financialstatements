package com.tekion.accounting.fs.service.excelGeneration;

import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.accounting.fs.service.common.excelGeneration.columnConfigs.ExcelColumnConfigGeneratorService;
import com.tekion.accounting.fs.service.common.excelGeneration.dto.AccExcelRequestDto;
import com.tekion.accounting.fs.service.common.excelGeneration.dto.SheetInfoDto;
import com.tekion.accounting.fs.service.common.excelGeneration.enums.ExcelReportType;
import com.tekion.accounting.fs.service.common.excelGeneration.reportRows.MemoWorksheetReportRow;
import com.tekion.core.excelGeneration.models.model.v2.DataFetchType;
import com.tekion.core.excelGeneration.models.model.v2.FetchNextBatchRequestV2;
import com.tekion.core.excelGeneration.models.model.v2.NextBatchDataV2;
import com.tekion.core.utils.UserContext;
import com.tekion.core.utils.UserContextProvider;
import com.tekion.core.validation.TValidator;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class MemoWorksheetExcelReportGeneratorTest extends TestCase {

    @InjectMocks
    MemoWorksheetExcelReportGenerator reportGenerator;

    @Mock
    MemoWorksheetExcelHelperService helperService;

    @Mock
    DealerConfig dealerConfig;

    @Mock
    TValidator validator;

    @Mock
    ExcelColumnConfigGeneratorService excelColumnConfigGeneratorService;

    @Before
    public void setUp() {
        UserContextProvider.setContext(new UserContext("id1", "tenant1", "dealer1"));
        Mockito.when(dealerConfig.getDealerTimeZoneName()).thenReturn("local");
    }


    @Test
    public void testCreateExcelGenerationDto() {
        Set<SheetInfoDto> sheetInfoDtoSet = new HashSet<>();
        sheetInfoDtoSet.add(getSheetInfoDto());
        Mockito.when(helperService.setSheetIdentifier(Mockito.any()))
                .thenReturn(sheetInfoDtoSet);
        Mockito.doNothing().when(excelColumnConfigGeneratorService).fixOrderOfColumnsInGeneratedList(Mockito.anyList());
        assertNotNull(reportGenerator.createExcelGenerationDto(getPayload(), "MEMO_WORKSHEET"));
        Mockito.verify(excelColumnConfigGeneratorService, Mockito.times(1))
                .fixOrderOfColumnsInGeneratedList(Mockito.anyList());
        Mockito.verify(helperService, Mockito.times(1)).setSheetIdentifier(Mockito.any());
    }


    @Test
    public void testSupportReportNames() {
       assertTrue(reportGenerator.supportedReportNames().contains(ExcelReportType.MEMO_WORKSHEET.name()));
    }


    @Test
    public void testDoOnFetchMoreRecordsCallback() {
        Mockito.when(helperService.getExportableReportRows(Mockito.anyString(), Mockito.any()))
                        .thenReturn(getMemoWorksheetReportRowList());
        FetchNextBatchRequestV2 fetchNextBatchRequestV2 = getFetchNextBatchRequestV2();
        fetchNextBatchRequestV2.setDataFetchType(DataFetchType.BOTTOM_ADDITIONAL_ROWS);
        NextBatchDataV2 response = reportGenerator.doOnFetchMoreRecordsCallback(fetchNextBatchRequestV2, "MEMO_WORKSHEET");
        assertEquals(0, response.getRowDataList().size());
    }


    private FetchNextBatchRequestV2 getFetchNextBatchRequestV2() {
        FetchNextBatchRequestV2 fetchNextBatchRequestV2 = new FetchNextBatchRequestV2();
        fetchNextBatchRequestV2.setSheetIdentifier("sheetId");
        fetchNextBatchRequestV2.setAwsRequestId("awsReqId");
        fetchNextBatchRequestV2.setReportType("MEMO_WORKSHEET");
        fetchNextBatchRequestV2.setDataFetchType(DataFetchType.DATA_RECORDS);
        fetchNextBatchRequestV2.setOriginalPayload(getPayload());
        return fetchNextBatchRequestV2;
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

    private SheetInfoDto getSheetInfoDto() {
        SheetInfoDto sheetInfoDto = new SheetInfoDto();
        sheetInfoDto.setSheetIdentifier("s1");
        sheetInfoDto.setComputedColumnConfigList(new ArrayList<>());
        return  sheetInfoDto;
    }

    private List<MemoWorksheetReportRow> getMemoWorksheetReportRowList() {
        List<MemoWorksheetReportRow> memoWorksheetReportRows = new ArrayList<>();
        MemoWorksheetReportRow memoWorksheetReportRow1 = new MemoWorksheetReportRow();
        memoWorksheetReportRow1.setFsPage("1");
        memoWorksheetReportRow1.setFsLine("1");
        memoWorksheetReportRow1.setStatus("s1");
        memoWorksheetReportRow1.setFieldType("numeric");

        MemoWorksheetReportRow memoWorksheetReportRow2 = new MemoWorksheetReportRow();
        memoWorksheetReportRow2.setFsPage("2");
        memoWorksheetReportRow2.setFsLine("2");
        memoWorksheetReportRow2.setStatus("s2");
        memoWorksheetReportRow2.setFieldType("numeric");
        memoWorksheetReportRows.add(memoWorksheetReportRow1);
        memoWorksheetReportRows.add(memoWorksheetReportRow2);
        return memoWorksheetReportRows;
    }

}
