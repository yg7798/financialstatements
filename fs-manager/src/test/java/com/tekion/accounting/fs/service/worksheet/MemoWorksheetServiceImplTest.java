package com.tekion.accounting.fs.service.worksheet;

import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.beans.memo.FieldType;
import com.tekion.accounting.fs.beans.memo.MemoValue;
import com.tekion.accounting.fs.beans.memo.MemoWorksheet;
import com.tekion.accounting.fs.beans.memo.MemoWorksheetTemplate;
import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.accounting.fs.common.utils.UserContextUtils;
import com.tekion.accounting.fs.dto.memo.CopyMemoValuesDto;
import com.tekion.accounting.fs.dto.memo.MemoBulkUpdateDto;
import com.tekion.accounting.fs.dto.memo.MemoWorkSheetUpdateDto;
import com.tekion.accounting.fs.dto.memo.WorksheetRequestDto;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.enums.OemCellDurationType;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.repos.worksheet.MemoWorksheetRepo;
import com.tekion.accounting.fs.repos.worksheet.MemoWorksheetTemplateRepo;
import com.tekion.core.utils.UserContext;
import com.tekion.core.utils.UserContextProvider;
import junit.framework.TestCase;
import org.assertj.core.util.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@PrepareForTest({UserContextUtils.class, System.class})
@RunWith(PowerMockRunner.class)
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
        PowerMockito.mockStatic(System.class);
        PowerMockito.when(System.getenv(anyString())).thenReturn("local");
    }

    @Test
    public void testGetMemoWorksheet() {
        Mockito.when(fsEntryRepo.findByIdAndDealerId(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(getFsEntry(), null, getFsEntry());
        Mockito.when(memoWorksheetRepo.findByFSId(anyString())).
                thenReturn(Stream.of(getMemoWorksheet1(),getMemoWorksheet2()).collect(Collectors.toList()), new ArrayList<>());
        Mockito.when(memoWorksheetTemplateRepo.findByOemYearAndCountry(anyString(), anyInt(), anyInt(), anyString()))
                        .thenReturn(memoWorksheetTemplates(Arrays.asList("1", "2")));
        Mockito.when(fsEntryRepo.findDefaultTypeWithoutNullCheck(anyString(), anyInt(), anyString(), anyString()))
                        .thenReturn(getFsEntry());

        assertEquals(Stream.of(getMemoWorksheet1(),getMemoWorksheet2()).collect(Collectors.toList()),
                memoWorksheetService.getMemoWorksheet("6155a7d8b3cb1f0006868cd6"));

        assertNull(memoWorksheetService.getMemoWorksheet("6155a7d8b3cb1f0006868cd6"));

        assertNotNull(memoWorksheetService.getMemoWorksheet("6155a7d8b3cb1f0006868cd6"));
    }

    @Test
    public void testGetMemoWorksheetsForExcel() {
        when(fsEntryRepo.findByIdAndDealerId(anyString(), anyString())).thenReturn(null, getFsEntry());
        assertNull(memoWorksheetService.getMemoWorksheetsForExcel("6155a7d8b3cb1f0006868cd6", 1, true));

        Mockito.when(memoWorksheetRepo.findByFSId(anyString())).
                thenReturn(Stream.of(getMemoWorksheet1(),getMemoWorksheet2()).collect(Collectors.toList()), new ArrayList<>());
        assertEquals(2, memoWorksheetService.getMemoWorksheetsForExcel("6155a7d8b3cb1f0006868cd6", 4, true).size());

        Mockito.when(memoWorksheetTemplateRepo.findByOemYearAndCountry(anyString(), anyInt(),
                anyInt(), anyString())).thenReturn(memoWorksheetTemplates(Arrays.asList("1","2")));
        assertEquals(0, memoWorksheetService.getMemoWorksheetsForExcel("6155a7d8b3cb1f0006868cd6", 4, false).size());
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
        Mockito.when(memoWorksheetRepo.findByIds(Mockito.anySet(), anyString())).
                thenReturn(Stream.of(getMemoWorksheet1(),getMemoWorksheet2()).collect(Collectors.toList()));
        List<MemoWorksheet> memoWorksheetList = memoWorksheetService.bulkUpdate(getMemoBulkUpdateDto());
        assertEquals(2, memoWorksheetList.size());
    }

    @Test
    public void testRemigrateFromTemplate() {
        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(anyString(), anyString()))
                .thenReturn(getFsEntry());
        Mockito.doNothing().when(memoWorksheetRepo).deleteWorkSheetsByFsId(anyString(),anyString());
        memoWorksheetService.remigrateFromTemplate("6155a7d8b3cb1f0006868cd6");
        Mockito.verify(fsEntryRepo, times(1)).findByIdAndDealerIdWithNullCheck(anyString(), anyString());
        Mockito.verify(memoWorksheetRepo, times(1)).deleteWorkSheetsByFsId(anyString(),anyString());
    }

    @Test
    public void testMigrateIfNotPresentFromTemplate() {
        Mockito.when(memoWorksheetRepo.findByFSId(anyString()))
                .thenReturn(Arrays.asList(getMemoWorksheet1(), getMemoWorksheet2()));
        memoWorksheetService.migrateIfNotPresentFromTemplate(getFsEntry());
        Mockito.verify(memoWorksheetRepo, times(1))
                .findByFSId(anyString());
    }

    @Test
    public void testMigrateMemoWorksheetFromOemToFSLevel() {
        when(fsEntryRepo.getFSEntries(anyString())).thenReturn(Arrays.asList(getFsEntry()));
       doNothing().when(memoWorksheetRepo).updateFsIdInMemoWorksheet(any());
       memoWorksheetService.migrateMemoWorksheetFromOemToFSLevel("2");
    }


    @Test
    public void testMigrateFieldTypeInMemoWorkSheet() {
        Mockito.when(memoWorksheetRepo.findByFSId(anyString()))
                .thenReturn(Arrays.asList(getMemoWorksheet1(), getMemoWorksheet2()));
        Mockito.when(memoWorksheetTemplateRepo.findByOemYearAndCountry(anyString(), anyInt(), anyInt(), anyString()))
                .thenReturn(memoWorksheetTemplates(Arrays.asList("1","2")));
        when(memoWorksheetRepo.updateBulk(anyList(), anyString())).thenReturn(new ArrayList<>());
        memoWorksheetService.migrateFieldTypeInMemoWorkSheet("6155a7d8b3cb1f0006868cd6",
                WorksheetRequestDto.builder().oemId(OEM.GM).version(1).year(2021).build());
    }

    @Test
    public void testDeleteMemoWorksheetsByKey() {
        assertEquals(0, memoWorksheetService.deleteMemoWorksheetsByKey(new HashSet<>(), null).size());

        Mockito.when(fsEntryRepo.findByIds(any(), anyString()))
                .thenReturn(Collections.singletonList(getFsEntry()));
        Mockito.when(memoWorksheetRepo.findByKeys(anyString(), any(), anyString()))
                .thenReturn(memoWorksheets(Arrays.asList("1","2")));
        Mockito.doNothing().when(memoWorksheetRepo).deleteMemoWorksheetsByKeys(any(), any(), anyString());
        Set<String> keys = Sets.newHashSet();
        keys.add("dummyKey");
        memoWorksheetService.deleteMemoWorksheetsByKey(keys, null);
    }

    @Test
    public void testUpdateActiveFieldsFromPreviousWorksheets() {
        Mockito.when(fsEntryRepo.findDefaultType(anyString(),anyInt(), anyString(),anyString()))
                .thenReturn(getFsEntry());
        Mockito.when(memoWorksheetRepo.findByFSId(anyString()))
                .thenReturn(memoWorksheets(Arrays.asList("1","2")),memoWorksheets(Arrays.asList("1","2")), new ArrayList<>());
        doNothing().when(memoWorksheetRepo).insertBulk(anyList());
        memoWorksheetService.updateActiveFieldsFromPreviousWorksheets(OEM.GM, 2020,2021, 1, "4");
        verify(fsEntryRepo, times(2)).findDefaultType(anyString(),anyInt(), anyString(),anyString());
        verify(memoWorksheetRepo, times(2)).findByFSId(anyString());

    }

    @Test
    public void testCopyValues() {
        CopyMemoValuesDto copyMemoValuesDto = CopyMemoValuesDto.builder()
                .oemId(OEM.GM)
                .version(1)
                .fromMonth(3)
                .fromYear(2020)
                .toYear(2020)
                .toMonth(4)
                .copyAllValues(true)
                .keys(Arrays.asList("dummyKey1", "dummyKey2"))
                .build();
        Mockito.when(fsEntryRepo.findDefaultType(anyString(),anyInt(), anyString(),anyString()))
                        .thenReturn(getFsEntry());
        Mockito.when(memoWorksheetTemplateRepo.findByOemYearAndCountry(anyString(), anyInt(), anyInt(), anyString()))
                        .thenReturn(memoWorksheetTemplates(Arrays.asList("1","2")));
        Mockito.when(memoWorksheetRepo.findByFSId(anyString()))
                        .thenReturn(memoWorksheets(Arrays.asList("1","2")));
        Mockito.when(memoWorksheetRepo.findByKeys(anyString(), any(), anyString()))
                        .thenReturn(memoWorksheets(Arrays.asList("1","2")));
        Mockito.when(memoWorksheetRepo.updateBulk(anyList(), anyString()))
                        .thenReturn(new ArrayList<>());
        assertEquals(2,memoWorksheetService.copyValues(copyMemoValuesDto).size());
        Mockito.verify(fsEntryRepo, times(2)).findDefaultType(anyString(),anyInt(), anyString(),anyString());
        Mockito.verify(memoWorksheetRepo, times(0)).findByKeys(anyString(), any(), anyString());

        reset(fsEntryRepo);
        reset(memoWorksheetRepo);
    }

    @Test
    public void testCopyValues_whenToYearAndFromYearAreDifferent() {
        CopyMemoValuesDto copyMemoValuesDto = CopyMemoValuesDto.builder()
                .oemId(OEM.GM)
                .version(1)
                .fromMonth(3)
                .fromYear(2020)
                .toYear(2021)
                .toMonth(4)
                .copyAllValues(true)
                .keys(Arrays.asList("dummyKey1", "dummyKey2"))
                .build();
        Mockito.when(fsEntryRepo.findDefaultType(anyString(),anyInt(), anyString(),anyString()))
                .thenReturn(getFsEntry());
        Mockito.when(memoWorksheetTemplateRepo.findByOemYearAndCountry(anyString(), anyInt(), anyInt(), anyString()))
                .thenReturn(memoWorksheetTemplates(Arrays.asList("1","2")));
        Mockito.when(memoWorksheetRepo.findByFSId(anyString()))
                .thenReturn(memoWorksheets(Arrays.asList("1","2")));
        Mockito.when(memoWorksheetRepo.findByKeys(anyString(), any(), anyString()))
                .thenReturn(memoWorksheets(Arrays.asList("1","2")));
        Mockito.when(memoWorksheetRepo.updateBulk(anyList(), anyString()))
                .thenReturn(new ArrayList<>());
        assertEquals(2,memoWorksheetService.copyValues(copyMemoValuesDto).size());
        Mockito.verify(fsEntryRepo, times(2)).findDefaultType(anyString(),anyInt(), anyString(),anyString());
        Mockito.verify(memoWorksheetRepo, times(0)).findByKeys(anyString(), any(), anyString());

        reset(fsEntryRepo);
        reset(memoWorksheetRepo);
    }

    @Test
    public void testCopyValues_whenToMemoSheetIsNull() {
        CopyMemoValuesDto copyMemoValuesDto = CopyMemoValuesDto.builder()
                .oemId(OEM.GM)
                .version(1)
                .fromMonth(3)
                .fromYear(2020)
                .toYear(2021)
                .toMonth(4)
                .copyAllValues(true)
                .keys(Arrays.asList("dummyKey1", "dummyKey2"))
                .build();
        Mockito.when(fsEntryRepo.findDefaultType(anyString(),anyInt(), anyString(),anyString()))
                .thenReturn(getFsEntry());
        Mockito.when(memoWorksheetTemplateRepo.findByOemYearAndCountry(anyString(), anyInt(), anyInt(), anyString()))
                .thenReturn(memoWorksheetTemplates(Arrays.asList("1","2")));
        Mockito.when(memoWorksheetRepo.findByFSId(anyString()))
                .thenReturn(memoWorksheets(Arrays.asList("1","2")), new ArrayList<>());
        Mockito.when(memoWorksheetRepo.findByKeys(anyString(), any(), anyString()))
                .thenReturn(memoWorksheets(Arrays.asList("1","2")));
        Mockito.when(memoWorksheetRepo.updateBulk(anyList(), anyString()))
                .thenReturn(new ArrayList<>());
        assertEquals(2,memoWorksheetService.copyValues(copyMemoValuesDto).size());
        Mockito.verify(fsEntryRepo, times(2)).findDefaultType(anyString(),anyInt(), anyString(),anyString());
        Mockito.verify(memoWorksheetRepo, times(0)).findByKeys(anyString(), any(), anyString());

        reset(fsEntryRepo);
        reset(memoWorksheetRepo);
    }

    @Test
    public void testMigrateForMissingKeys() {

        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(anyString(), anyString()))
                .thenReturn(getFsEntry());
        Mockito.when(memoWorksheetRepo.findByFSId(anyString())).thenReturn(memoWorksheets(Arrays.asList("1","3")));
        Mockito.when(memoWorksheetTemplateRepo.findByOemYearAndCountry(anyString(), anyInt(), anyInt(), anyString()))
                .thenReturn(memoWorksheetTemplates(Arrays.asList("1", "2")));

        assertEquals(1, memoWorksheetService.migrateForMissingKeys("6155a7d8b3cb1f0006868cd6").size());
    }

    @Test
    public void testMigrateForMissingKeysAll() {
        Mockito.when(fsEntryRepo.getFsEntriesByOemIds(anyList(), anyInt(), anyString())).thenReturn(Collections.singletonList(getFsEntry()));
        Mockito.when(memoWorksheetTemplateRepo.findByOemYearAndCountry(anyString(), anyInt(), anyInt(), anyString()))
                .thenReturn(memoWorksheetTemplates(Arrays.asList("1","3")));
        Mockito.when(memoWorksheetRepo.findByFsIds(anyCollection())).thenReturn(memoWorksheets(Arrays.asList("1","2"), "6155a7d8b3cb1f0006868cd6"));
        memoWorksheetService.migrateForMissingKeysForAll("Acura", 2021, "us");
        final ArgumentCaptor<List<MemoWorksheet>> argumentCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(memoWorksheetRepo).insertBulk(argumentCaptor.capture());
        List<MemoWorksheet> capturedArgument = argumentCaptor.getValue();
        assertEquals(1, capturedArgument.size());
    }



    private MemoWorksheet getMemoWorksheet1(){
        MemoWorksheet memoWorksheet=new MemoWorksheet();
        memoWorksheet.setFsId("6155a7d8b3cb1f0006868cd6");
        memoWorksheet.setKey("x");
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

    private MemoWorksheet memoWorksheet(String key){
        MemoValue memoValue1 = new MemoValue();
        memoValue1.setMonth(3);
        memoValue1.setYtdValue(BigDecimal.ZERO);
        memoValue1.setMtdValue(BigDecimal.ZERO);

        MemoValue memoValue2 = new MemoValue();
        memoValue2.setMonth(4);
        memoValue2.setYtdValue(BigDecimal.TEN);
        memoValue2.setMtdValue(BigDecimal.TEN);
        return MemoWorksheet.builder().key(key).values(Arrays.asList(memoValue1, memoValue2)).build();
    }

    private MemoWorksheetTemplate memoWorksheetTemplate(String key){
        Set<OemCellDurationType> oemCellDurationTypes = new HashSet<>();
        oemCellDurationTypes.add(OemCellDurationType.MTD);
        return MemoWorksheetTemplate.builder().key(key).oemId("GM").durationTypes(oemCellDurationTypes).fieldType(FieldType.BALANCE).build();
    }

    private List<MemoWorksheetTemplate> memoWorksheetTemplates(Collection<String> ids){
        return ids.stream().map(this::memoWorksheetTemplate).collect(Collectors.toList());
    }

    private List<MemoWorksheet> memoWorksheets(Collection<String> ids){
        return ids.stream().map(this::memoWorksheet).collect(Collectors.toList());
    }
    private List<MemoWorksheet> memoWorksheets(Collection<String> ids, String fsId){
        List<MemoWorksheet> worksheets = memoWorksheets(ids);
        worksheets.forEach(x -> x.setFsId(fsId));
        return worksheets;
    }

    private MemoWorksheet getMemoWorksheet2(){
        MemoWorksheet memoWorksheet = new MemoWorksheet();

        memoWorksheet.setId("WSid2");
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
        memoValue1.setMonth(1);
        memoValue1.setMtdValue(BigDecimal.ZERO);
        memoValue1.setYtdValue(BigDecimal.ZERO);

        MemoValue memoValue2 = new MemoValue();
        memoValue2.setMonth(2);
        memoValue2.setMtdValue(BigDecimal.ONE);
        memoValue2.setYtdValue(BigDecimal.TEN);

        memoWorkSheetUpdateDto1.setValues(Stream.of(memoValue1,memoValue2).collect(Collectors.toList()));

        MemoWorkSheetUpdateDto memoWorkSheetUpdateDto2 =  new MemoWorkSheetUpdateDto();
        memoWorkSheetUpdateDto2.setId("WSid2");
        memoWorkSheetUpdateDto2.setBegBalance(BigDecimal.ZERO);

        MemoValue memoValue3 = new MemoValue();
        memoValue3.setMonth(1);
        memoValue3.setMtdValue(BigDecimal.ZERO);
        memoValue3.setYtdValue(BigDecimal.ZERO);

        MemoValue memoValue4 = new MemoValue();
        memoValue4.setMonth(2);
        memoValue4.setMtdValue(BigDecimal.ZERO);
        memoValue4.setYtdValue(BigDecimal.ZERO);

        memoWorkSheetUpdateDto2.setValues(Stream.of(memoValue3,memoValue4).collect(Collectors.toList()));

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