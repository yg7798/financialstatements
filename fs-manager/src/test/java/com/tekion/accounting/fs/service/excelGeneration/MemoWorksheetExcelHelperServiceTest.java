package com.tekion.accounting.fs.service.excelGeneration;

import com.google.common.collect.Maps;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.beans.memo.MemoValue;
import com.tekion.accounting.fs.beans.memo.MemoWorksheet;
import com.tekion.accounting.fs.beans.memo.MemoWorksheetTemplate;
import com.tekion.accounting.fs.enums.OemCellDurationType;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.service.common.excelGeneration.dto.SheetInfoDto;
import com.tekion.accounting.fs.service.common.excelGeneration.generators.financialStatement.dto.MemoWorksheetExcelRequestDto;
import com.tekion.accounting.fs.service.common.excelGeneration.generators.financialStatement.dto.MemoWorksheetRequestDto;
import com.tekion.accounting.fs.service.common.excelGeneration.generators.financialStatement.dto.WorksheetApplicableFilter;
import com.tekion.accounting.fs.service.common.excelGeneration.helper.ExcelReportGeneratorHelper;
import com.tekion.accounting.fs.service.common.excelGeneration.reportRows.MemoWorksheetReportRow;
import com.tekion.accounting.fs.service.worksheet.MemoWorksheetService;
import com.tekion.accounting.fs.service.worksheet.MemoWorksheetTemplateService;
import com.tekion.core.excelGeneration.models.model.ColumnConfig;
import com.tekion.core.excelGeneration.models.model.v2.FetchNextBatchRequestV2;
import com.tekion.core.excelGeneration.models.model.v2.SheetDetails;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(MockitoJUnitRunner.Silent.class)
public class MemoWorksheetExcelHelperServiceTest extends TestCase {

    @Mock
    MemoWorksheetService memoWorksheetService;

    @Mock
    MemoWorksheetTemplateService memoWorksheetTemplateService;

    @Mock
    ExcelReportGeneratorHelper helper;

    @Mock
    FSEntryRepo fsEntryRepo;

    @InjectMocks
    MemoWorksheetExcelHelperService helperService;

    @Test
    public void testGetExportableReportRows() {
        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(Mockito.any(), Mockito.any()))
                .thenReturn(getFsEntry());
        Mockito.when(memoWorksheetTemplateService.getMemoWorksheetTemplates(Mockito.any(), Mockito.anyInt(), Mockito.anyInt()))
                        .thenReturn(getMemoWorksheetTemplateList());
        Mockito.when(memoWorksheetService.getMemoWorksheetsForExcel(Mockito.any(), Mockito.anyInt(), Mockito.anyBoolean()))
                        .thenReturn(Arrays.asList(getMemoWorksheet1()));
        helperService.getExportableReportRows("MEMO_WORKSHEET", getWorksheetExcelReportContext());
        Mockito.verify(fsEntryRepo, Mockito.times(1)).findByIdAndDealerIdWithNullCheck(Mockito.any(), Mockito.any());
        Mockito.verify(memoWorksheetService, Mockito.times(1)).getMemoWorksheetsForExcel(Mockito.any(), Mockito.anyInt(), Mockito.anyBoolean());
        Mockito.verify(memoWorksheetTemplateService, Mockito.times(1)).getMemoWorksheetTemplates(Mockito.any(), Mockito.anyInt(), Mockito.anyInt());

    }

    @Test
    public void testCreateMemoWorksheetReportRows() {
        assertNotNull(helperService.createMemoWorksheetReportRows(getWorksheetExcelReportContext()));
    }

    @Test
    public void testSetSheetIdentifier() {
        assertNotNull(helperService.setSheetIdentifier(new SheetDetails()));
    }

    @Test
    public void testGetSingleRowDataList() {
        Mockito.when(helper.getCopyOfColumnConfig(Mockito.any()))
                        .thenReturn(getSheetInfoDto().getComputedColumnConfigList().get(0),
                                getSheetInfoDto().getComputedColumnConfigList().get(1));
        helperService.getSingleRowDataList(getMemoWorksheetReportRowList(), getSheetInfoDto());
    }

    private List<MemoWorksheetReportRow> getMemoWorksheetReportRowList() {
        List<MemoWorksheetReportRow> memoWorksheetReportRows = new ArrayList<>();
        MemoWorksheetReportRow memoWorksheetReportRow1 = new MemoWorksheetReportRow();
        memoWorksheetReportRow1.setFsPage("1");
        memoWorksheetReportRow1.setFsLine("1");
        memoWorksheetReportRow1.setStatus("Active");
        memoWorksheetReportRow1.setFieldType("numeric");

        MemoWorksheetReportRow memoWorksheetReportRow2 = new MemoWorksheetReportRow();
        memoWorksheetReportRow2.setFsPage("2");
        memoWorksheetReportRow2.setFsLine("2");
        memoWorksheetReportRow2.setStatus("Active");
        memoWorksheetReportRow2.setFieldType("numeric");
        memoWorksheetReportRows.add(memoWorksheetReportRow1);
        memoWorksheetReportRows.add(memoWorksheetReportRow2);
        return memoWorksheetReportRows;
    }

    private SheetInfoDto getSheetInfoDto() {
        List<ColumnConfig> columnConfigList = new ArrayList<>();
        ColumnConfig columnConfig1 = new ColumnConfig();
        columnConfig1.setKey("mtdValue");

        ColumnConfig columnConfig2 = new ColumnConfig();
        columnConfig1.setKey("ytdValue");
        columnConfigList.add(columnConfig1);
        columnConfigList.add(columnConfig2);

        SheetInfoDto sheetInfoDto = new SheetInfoDto();
        sheetInfoDto.setSheetIdentifier("s1");
        sheetInfoDto.setComputedColumnConfigList(columnConfigList);
        return  sheetInfoDto;
    }

    private WorksheetExcelReportContext getWorksheetExcelReportContext() {

        WorksheetApplicableFilter filter = new WorksheetApplicableFilter();
        filter.setKey("STATUS");
        filter.setValues(Arrays.asList("Active"));

        MemoWorksheetRequestDto memoWorksheetRequestDto =
               new MemoWorksheetRequestDto();
        memoWorksheetRequestDto.setMonth(1);
        memoWorksheetRequestDto.setApplicableFilters(Arrays.asList(filter));

        MemoWorksheetExcelRequestDto requestDto =
                new MemoWorksheetExcelRequestDto();
        requestDto.setMemoWorksheetRequestDto(memoWorksheetRequestDto);
        requestDto.setSheetInfoDtoSet(new HashSet<>());
        requestDto.setTimeStampOfGeneration(79379437593L);

        FetchNextBatchRequestV2 fetchNextBatchRequestV2 =
                new FetchNextBatchRequestV2();
        fetchNextBatchRequestV2.setOriginalPayload(requestDto);

        WorksheetExcelReportContext context =
                WorksheetExcelReportContext.builder()
                        .reportRequestDto(requestDto)
                        .keyToMemoWorksheetMap(getKeyToMemoWorksheet())
                        .nextBatchRequestV2(fetchNextBatchRequestV2)
                        .reportType("MEMO_WORKSHEET")
                        .memoWorksheetTemplates(getMemoWorksheetTemplateList())
                        .build();
        return context;
    }

    private Map<String, MemoWorksheet> getKeyToMemoWorksheet() {

        List<MemoValue> memoValues = new ArrayList<>();
        MemoValue memoValue1 = new MemoValue();
        memoValue1.setMonth(1);
        memoValue1.setYtdValue(new BigDecimal(10));
        memoValue1.setMtdValue(new BigDecimal(5));
        memoValues.add(memoValue1);

        MemoValue memoValue2 = new MemoValue();
        memoValue2.setMonth(1);
        memoValue2.setYtdValue(new BigDecimal(10));
        memoValue2.setMtdValue(new BigDecimal(5));
        memoValues.add(memoValue2);

        MemoWorksheet memoWorksheet1 = new MemoWorksheet();
        memoWorksheet1.setKey("fsPage");
        memoWorksheet1.setDealerId("4");
        memoWorksheet1.setOemId("GM");
        memoWorksheet1.setFieldType("BALANCE");
        memoWorksheet1.setVersion(1);
        memoWorksheet1.setYear(2021);
        memoWorksheet1.setValues(memoValues);

        MemoWorksheet memoWorksheet2 = new MemoWorksheet();
        memoWorksheet2.setKey("fsLine");
        memoWorksheet2.setDealerId("4");
        memoWorksheet2.setOemId("GM");
        memoWorksheet2.setFieldType("COUNT");
        memoWorksheet2.setVersion(1);
        memoWorksheet2.setYear(2021);
        memoWorksheet2.setValues(memoValues);

        Map<String, MemoWorksheet> map = Maps.newHashMap();
        map.put("fsPage", memoWorksheet1);
        map.put("fsLine", memoWorksheet2);
        return map;
    }

    private FSEntry getFsEntry() {
        FSEntry fsEntry = new FSEntry();
        fsEntry.setDealerId("5");
        fsEntry.setYear(2021);
        fsEntry.setVersion(1);
        fsEntry.setOemId("Acura");
        fsEntry.setId("6155a7d8b3cb1f0006868cd6");
        fsEntry.setSiteId("-1_5");
        fsEntry.setFsType("INTERNAL");
        return fsEntry;
    }

    private MemoWorksheet getMemoWorksheet1(){
        MemoWorksheet memoWorksheet=new MemoWorksheet();
        memoWorksheet.setFsId("6155a7d8b3cb1f0006868cd6");
        memoWorksheet.setKey("fsPage");
        memoWorksheet.setOemId("Acura");
        memoWorksheet.setYear(2021);
        memoWorksheet.setVersion(2);
        memoWorksheet.setId("WSid1");

        MemoValue memoValue1 = new MemoValue();
        memoValue1.setMonth(3);
        memoValue1.setMtdValue(BigDecimal.ZERO);
        memoValue1.setYtdValue(BigDecimal.ONE);

        MemoValue memoValue2 = new MemoValue();
        memoValue2.setMonth(4);
        memoValue2.setMtdValue(BigDecimal.ONE);
        memoValue2.setYtdValue(BigDecimal.TEN);

        memoWorksheet.setValues(Stream.of(memoValue1,memoValue2).collect(Collectors.toList()));
        return memoWorksheet;
    }

    private List<MemoWorksheetTemplate> getMemoWorksheetTemplateList() {
        Set<OemCellDurationType> oemCellDurationTypeSet = Sets.newSet();
        oemCellDurationTypeSet.add(OemCellDurationType.MTD);
        oemCellDurationTypeSet.add(OemCellDurationType.YTD);

        List<MemoWorksheetTemplate> memoWorksheetTemplateList = new ArrayList<>();
        MemoWorksheetTemplate memoWorksheetTemplate1 = MemoWorksheetTemplate.builder()
                .country("US")
                .key("fsPage")
                .oemId("GM")
                .year(2020)
                .name("FS Page")
                .version(1)
                .durationTypes(oemCellDurationTypeSet)
                .build();
        MemoWorksheetTemplate memoWorksheetTemplate2 = MemoWorksheetTemplate.builder()
                .country("US")
                .key("GM")
                .year(2020)
                .oemId("fsLine")
                .name("FS line")
                .durationTypes(oemCellDurationTypeSet)
                .version(1)
                .build();
        memoWorksheetTemplateList.add(memoWorksheetTemplate1);
        memoWorksheetTemplateList.add(memoWorksheetTemplate2);
        return memoWorksheetTemplateList;
    }
}
