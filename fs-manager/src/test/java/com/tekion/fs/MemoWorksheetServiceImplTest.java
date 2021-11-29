package com.tekion.fs;

import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.beans.memo.FieldType;
import com.tekion.accounting.fs.beans.memo.MemoValue;
import com.tekion.accounting.fs.beans.memo.MemoWorksheet;
import com.tekion.accounting.fs.beans.memo.MemoWorksheetTemplate;
import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.accounting.fs.common.utils.UserContextUtils;
import com.tekion.accounting.fs.dto.memo.MemoBulkUpdateDto;
import com.tekion.accounting.fs.dto.memo.MemoWorkSheetUpdateDto;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.repos.worksheet.MemoWorksheetRepo;
import com.tekion.accounting.fs.repos.worksheet.MemoWorksheetTemplateRepo;
import com.tekion.accounting.fs.service.worksheet.MemoWorksheetServiceImpl;
import com.tekion.core.utils.UserContext;
import com.tekion.core.utils.UserContextProvider;
import junit.framework.TestCase;
import org.apache.commons.compress.utils.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.powermock.core.classloader.annotations.PrepareForTest;

@PrepareForTest(UserContextUtils.class)
@RunWith(MockitoJUnitRunner.Silent.class)
public class MemoWorksheetServiceImplTest extends TestCase {

    @InjectMocks
    MemoWorksheetServiceImpl memoWorksheetService;

    @Mock
    MemoWorksheetRepo memoWorksheetRepo;
    @Mock
    DealerConfig dealerConfig;
    @Mock
    MemoWorksheetTemplateRepo memoWorksheetTemplateRepo;
    @Mock
    FSEntryRepo fsEntryRepo;
    @Captor
    private ArgumentCaptor<ArrayList<MemoWorksheet>> worksheetsCaptor;

    @Before
    public void setUp() {
        UserContextProvider.setContext(new UserContext("-1", "ca", "4"));
        Mockito.when(dealerConfig.getDealerCountryCode()).thenReturn("US");
        //System.set(, "true");
    }

    @Test
    public void testGetMemoWorksheet() {
        Mockito.when(fsEntryRepo.findByIdAndDealerId(Mockito.anyString(), Mockito.anyString())).thenReturn(getFsEntry());
        Mockito.when(memoWorksheetRepo.findByFSId(anyString())).
                thenReturn(Stream.of(getMemoWorksheet1(),getMemoWorksheet2()).collect(Collectors.toList()));
        assertEquals(Stream.of(getMemoWorksheet1(),getMemoWorksheet2()).collect(Collectors.toList()),
                memoWorksheetService.getMemoWorksheet("6155a7d8b3cb1f0006868cd6"));
    }

    @Test
    public void testSave() {
        MemoWorksheet memoWorksheet = getMemoWorksheet1();
        Mockito.when(memoWorksheetRepo.findById(anyString())).
                thenReturn(memoWorksheet);
        Mockito.when(memoWorksheetRepo.save(memoWorksheet)).
                thenReturn(memoWorksheet);
        assertEquals(memoWorksheet, memoWorksheetService.save(memoWorksheet));
    }

    @Test // do not save duplicate memo worksheets
    public void migrateWorksheetsForSelectedTemplates() {
        Set<String> sheetKeys = new HashSet<>();
        sheetKeys.add("k1");

        Set<String> templateKeys = new HashSet<>();
        templateKeys.add("k2");

        Set<String> totalKeys = new HashSet<>();
        totalKeys.add("k1");
        totalKeys.add("k2");

        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(anyString(), anyString())).thenReturn(getFsEntry());
        Mockito.when(memoWorksheetRepo.findByKeys(anyString(), anyCollection(), anyString())).thenReturn(memoWorksheets(sheetKeys));
        Mockito.when(memoWorksheetTemplateRepo.findByOemYearAndCountry(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyCollection(), Mockito.anyString())).thenReturn(memoWorksheetTemplates(templateKeys));

        Assert.assertEquals(1, memoWorksheetService.migrateMemoWorksheetsForKeys("6155a7d8b3cb1f0006868cd6", totalKeys).size());
        verify(memoWorksheetRepo, times(1)).insertBulk(anyList());
        verify(memoWorksheetRepo).insertBulk(worksheetsCaptor.capture());
        List<MemoWorksheet> worksheetsToSave = worksheetsCaptor.getValue();
        assertEquals(worksheetsToSave.size(), 1);
        assertEquals(worksheetsToSave.get(0).getKey(), "k2");
    }

    @Test
    public void testBulkUpdate() {
        Mockito.when(memoWorksheetRepo.findByIds(Mockito.anyList(), anyString())).
                thenReturn(Stream.of(getMemoWorksheet1(),getMemoWorksheet2()).collect(Collectors.toList()));
        List<MemoWorksheet> memoWorksheetList =memoWorksheetService.bulkUpdate(getMemoBulkUpdateDto());
        assertNotNull(memoWorksheetList);
    }


    private MemoWorksheet getMemoWorksheet1(){
        MemoWorksheet memoWorksheet=new MemoWorksheet();
        memoWorksheet.setFsId("6155a7d8b3cb1f0006868cd6");
        memoWorksheet.setKey("x");
        memoWorksheet.setOemId("Acura");
        memoWorksheet.setYear(2021);
        memoWorksheet.setVersion(2);
        memoWorksheet.setId("6155a7d8b3cb1f0006868cd6");

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

    private MemoWorksheet memoWorksheet(String key){
        return MemoWorksheet.builder().key(key).build();
    }

    private MemoWorksheetTemplate memoWorksheetTemplate(String key){
        return MemoWorksheetTemplate.builder().key(key).fieldType(FieldType.BALANCE).build();
    }

    private List<MemoWorksheetTemplate> memoWorksheetTemplates(Collection<String> ids){
        return ids.stream().map(this::memoWorksheetTemplate).collect(Collectors.toList());
    }

    private List<MemoWorksheet> memoWorksheets(Collection<String> ids){
        return ids.stream().map(this::memoWorksheet).collect(Collectors.toList());
    }

    private MemoWorksheet getMemoWorksheet2(){
        MemoWorksheet memoWorksheet = new MemoWorksheet();

        memoWorksheet.setId("id2");
        memoWorksheet.setDealerId("4");
        memoWorksheet.setKey("key2");
        memoWorksheet.setOemId(OEM.GM.getOem());
        memoWorksheet.setFieldType(FieldType.COUNT.name());
        memoWorksheet.setVersion(3);
        memoWorksheet.setYear(2021);
        memoWorksheet.setCreatedByUserId("user4");
        memoWorksheet.setModifiedByUserId("user3");

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

    private MemoBulkUpdateDto getMemoBulkUpdateDto(){
        MemoBulkUpdateDto memoBulkUpdateDto = new MemoBulkUpdateDto();

        MemoWorkSheetUpdateDto memoWorkSheetUpdateDto1 =  new MemoWorkSheetUpdateDto();
        memoWorkSheetUpdateDto1.setId("WSid1");
        memoWorkSheetUpdateDto1.setBegBalance(BigDecimal.ONE);

        MemoValue memoValue1 = new MemoValue();
        memoValue1.setMonth(3);
        memoValue1.setMtdValue(BigDecimal.ZERO);
        memoValue1.setYtdValue(BigDecimal.ZERO);

        MemoValue memoValue2 = new MemoValue();
        memoValue2.setMonth(4);
        memoValue2.setMtdValue(BigDecimal.ONE);
        memoValue2.setYtdValue(BigDecimal.TEN);

        memoWorkSheetUpdateDto1.setValues(Stream.of(memoValue1,memoValue2).collect(Collectors.toList()));

        MemoWorkSheetUpdateDto memoWorkSheetUpdateDto2 =  new MemoWorkSheetUpdateDto();
        memoWorkSheetUpdateDto2.setId("WSid2");
        memoWorkSheetUpdateDto1.setBegBalance(BigDecimal.ZERO);

        MemoValue memoValue3 = new MemoValue();
        memoValue1.setMonth(1);
        memoValue1.setMtdValue(BigDecimal.ZERO);
        memoValue1.setYtdValue(BigDecimal.ZERO);

        MemoValue memoValue4 = new MemoValue();
        memoValue2.setMonth(1);
        memoValue2.setMtdValue(BigDecimal.ZERO);
        memoValue2.setYtdValue(BigDecimal.ZERO);

        memoWorkSheetUpdateDto1.setValues(Stream.of(memoValue3,memoValue4).collect(Collectors.toList()));

        memoBulkUpdateDto.setMemoWorksheets(Stream.of(memoWorkSheetUpdateDto1,memoWorkSheetUpdateDto2).collect(Collectors.toList()));

        return memoBulkUpdateDto;
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
        return fsEntry;
    }




}