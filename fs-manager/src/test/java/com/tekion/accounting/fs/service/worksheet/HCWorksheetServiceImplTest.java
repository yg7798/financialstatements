package com.tekion.accounting.fs.service.worksheet;

import com.tekion.accounting.commons.dealer.DealerConfig;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.beans.memo.HCDepartment;
import com.tekion.accounting.fs.beans.memo.HCValue;
import com.tekion.accounting.fs.beans.memo.HCWorksheet;
import com.tekion.accounting.fs.beans.memo.HCWorksheetTemplate;
import com.tekion.accounting.fs.common.utils.UserContextUtils;
import com.tekion.accounting.fs.dto.memo.CopyHCWorksheetValuesDto;
import com.tekion.accounting.fs.dto.memo.HCBulkUpdateDto;
import com.tekion.accounting.fs.dto.memo.HCUpdateDto;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.repos.worksheet.HCWorksheetRepo;
import com.tekion.accounting.fs.repos.worksheet.HCWorksheetTemplateRepo;
import com.tekion.core.utils.UserContext;
import com.tekion.core.utils.UserContextProvider;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.context.event.annotation.AfterTestMethod;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;

@PrepareForTest(UserContextUtils.class)
@RunWith(PowerMockRunner.class)
public class HCWorksheetServiceImplTest extends TestCase {


    @InjectMocks
    HCWorksheetServiceImpl hcWorksheetService;

    @Mock
    HCWorksheetTemplateRepo hcWorksheetTemplateRepo;
    @Mock
    HCWorksheetRepo hcWorksheetRepo;
    @Mock
    DealerConfig dealerConfig;
    @Mock
    FSEntryRepo fsEntryRepo;

    @Before
    public void setUp() {
        UserContextProvider.setContext(new UserContext("-1", "ca", "4"));
        Mockito.when(dealerConfig.getDealerTimeZone()).thenReturn(TimeZone.getTimeZone("America/Los_Angeles"));
        Mockito.when(dealerConfig.getDealerCountryCode()).thenReturn("US");
        PowerMockito.mockStatic(System.class);
        PowerMockito.when(System.getenv(Mockito.anyString())).thenReturn("local");
    }

    @AfterTestMethod
    public void cleanUp() {
        reset(hcWorksheetRepo);
        reset(fsEntryRepo);
        reset(hcWorksheetTemplateRepo);
    }

    @Test
    public void testGetHCWorksheetTemplates() {
        Mockito.when(hcWorksheetTemplateRepo.findForOemByYearAndCountry(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString())).
                thenReturn(getHCWorksheetTemplate2());
        assertEquals(getHCWorksheetTemplate2(), hcWorksheetService.getHCWorksheetTemplate(OEM.GM, 2021, 1));
    }

    @Test
    public void testGetHCWorksheetsForYear() {
        List<HCWorksheet> hcWorksheetList = new ArrayList<HCWorksheet>() {{
            add(getHCWorksheet1());
            add(getHCWorksheet2());
        }};

        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(Mockito.anyString(), Mockito.anyString())).thenReturn(getFsEntry());
        Mockito.when(hcWorksheetRepo.findByFsId(Mockito.anyString())).
                thenReturn(Stream.of(getHCWorksheet1(), getHCWorksheet2()).collect(Collectors.toList()));
        assertEquals(hcWorksheetList, hcWorksheetService.getHCWorksheets("6155a7d8b3cb1f0006868cd6"));
    }

    @Test
    public void testSave() {
        HCWorksheetTemplate hcWorksheetTemplate = getHCWorksheetTemplate1();
        Mockito.when(hcWorksheetTemplateRepo.save(hcWorksheetTemplate)).
                thenReturn(hcWorksheetTemplate);
        assertEquals(hcWorksheetTemplate, hcWorksheetService.save(hcWorksheetTemplate));
    }

    @Test
    public void testBulkUpdate() {
        Mockito.when(hcWorksheetRepo.findByIds(Mockito.anyList(), Mockito.anyString())).
                thenReturn(Stream.of(getHCWorksheet1(),getHCWorksheet2()).collect(Collectors.toList()));
        List <HCWorksheet> hcWorksheetList = hcWorksheetService.bulkUpdate(getHCBulkUpdateDto());
        assertEquals(2,hcWorksheetList.size());
    }

    @Test
    public void testMigrateFromTemplate() {
        Mockito.when(fsEntryRepo.findDefaultType(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString())).thenReturn(getFsEntry());
        Mockito.when(hcWorksheetTemplateRepo.findForOemByYearAndCountry(Mockito.anyString(),Mockito.anyInt(),Mockito.anyInt(), Mockito.anyString())).
                thenReturn(getHCWorksheetTemplate1());
        List <HCWorksheet> hcWorksheetList = hcWorksheetService.migrateFromTemplate(OEM.GM,2020,1);
        assertEquals(2,hcWorksheetList.size());
    }

    @Test
    public void testCopyValues() {
        CopyHCWorksheetValuesDto copyHCValuesDto = CopyHCWorksheetValuesDto.builder()
                .oemId(OEM.GM)
                .version(1)
                .fromMonth(1)
                .fromYear(2020)
                .toYear(2020)
                .toMonth(2)
                .build();
        Mockito.when(fsEntryRepo.findDefaultType(anyString(),anyInt(), anyString(),anyString()))
                .thenReturn(getFsEntry());
        Mockito.when(hcWorksheetRepo.findByFsId(anyString()))
                .thenReturn(Arrays.asList(getHCWorksheet1(), getHCWorksheet2()));

        Mockito.when(hcWorksheetRepo.updateBulk(anyList(), anyString()))
                .thenReturn(new ArrayList<>());
        assertEquals(2,hcWorksheetService.copyValues(copyHCValuesDto).size());
        Mockito.verify(fsEntryRepo, times(2)).findDefaultType(anyString(),anyInt(), anyString(),anyString());
        Mockito.verify(hcWorksheetRepo, times(1)).findByFsId(anyString());
    }

    @Test
    public void testCopyValues_whenToMonthAndFromMonthAreDiff() {
        CopyHCWorksheetValuesDto copyHCValuesDto = CopyHCWorksheetValuesDto.builder()
                .oemId(OEM.GM)
                .version(1)
                .fromMonth(1)
                .fromYear(2020)
                .toYear(2021)
                .toMonth(2)
                .build();
        Mockito.when(fsEntryRepo.findDefaultType(anyString(),anyInt(), anyString(),anyString()))
                .thenReturn(getFsEntry());
        Mockito.when(hcWorksheetRepo.findByFsId(anyString()))
                .thenReturn(Arrays.asList(getHCWorksheet1(), getHCWorksheet2()));

        Mockito.when(hcWorksheetRepo.updateBulk(anyList(), anyString()))
                .thenReturn(new ArrayList<>());
        assertEquals(2,hcWorksheetService.copyValues(copyHCValuesDto).size());
        Mockito.verify(fsEntryRepo, times(2)).findDefaultType(anyString(),anyInt(), anyString(),anyString());
        Mockito.verify(hcWorksheetRepo, times(2)).findByFsId(anyString());
    }

    @Test
    public void testCopyValues_whenToHCSheetIsNull() {
        CopyHCWorksheetValuesDto copyHCValuesDto = CopyHCWorksheetValuesDto.builder()
                .oemId(OEM.GM)
                .version(1)
                .fromMonth(3)
                .fromYear(2020)
                .toYear(2021)
                .toMonth(4)
                .build();
        Mockito.when(fsEntryRepo.findDefaultType(anyString(),anyInt(), anyString(),anyString()))
                .thenReturn(getFsEntry());
        Mockito.when(hcWorksheetRepo.findByFsId(anyString()))
                .thenReturn(Arrays.asList(getHCWorksheet1()), new ArrayList<>());
        Mockito.when(hcWorksheetTemplateRepo.findForOemByYearAndCountry(anyString(), anyInt(), anyInt(), anyString()))
                        .thenReturn(getHCWorksheetTemplate1());
        assertEquals(2,hcWorksheetService.copyValues(copyHCValuesDto).size());
        Mockito.verify(fsEntryRepo, times(2)).findDefaultType(anyString(),anyInt(), anyString(),anyString());

        reset(fsEntryRepo);
        reset(hcWorksheetRepo);
    }


    @Test
    public void testMigrateHeadCountWorksheetFromOemToFSLevel() {
        Mockito.when(fsEntryRepo.getFSEntries(anyString()))
                .thenReturn(Arrays.asList(getFsEntry()));
        Mockito.doNothing().when(hcWorksheetRepo).updateFsIdInHCWorksheets(any());
        hcWorksheetService.migrateHeadCountWorksheetFromOemToFSLevel("4");
        Mockito.verify(fsEntryRepo, times(1)).getFSEntries(anyString());
        Mockito.verify(hcWorksheetRepo).updateFsIdInHCWorksheets(any());
    }

    @Test
    public void testMigrateFromTemplateWithFsId() {
        Mockito.when(hcWorksheetTemplateRepo.findForOemByYearAndCountry(anyString(), anyInt(), anyInt(), anyString()))
                .thenReturn(getHCWorksheetTemplate1());
        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(anyString(), anyString()))
                .thenReturn(getFsEntry());
        Mockito.doNothing().when(hcWorksheetRepo).insertBulk(any());
        hcWorksheetService.migrateFromTemplateWithFsId(OEM.GM, 2021, 1, "6155a7d8b3cb1f0006868cd6");
        Mockito.verify(hcWorksheetTemplateRepo,times(1)).findForOemByYearAndCountry(anyString(), anyInt(), anyInt(), anyString());
        Mockito.verify(fsEntryRepo, times(1)).findByIdAndDealerIdWithNullCheck(anyString(), anyString());
        Mockito.verify(hcWorksheetRepo, times(1)).insertBulk(any());
    }

    private HCWorksheetTemplate getHCWorksheetTemplate1() {
        HCWorksheetTemplate hcWorksheetTemplate = new HCWorksheetTemplate();
        hcWorksheetTemplate.setOemId(OEM.GM.getOem());
        hcWorksheetTemplate.setPrecision(2);
        hcWorksheetTemplate.setYear(2020);
        hcWorksheetTemplate.setVersion(1);
        hcWorksheetTemplate.setDepartments(Stream.of(getHCDepartment()).collect(Collectors.toList()));
        return hcWorksheetTemplate;
    }

    private HCWorksheetTemplate getHCWorksheetTemplate2() {
        HCWorksheetTemplate hcWorksheetTemplate = new HCWorksheetTemplate();
        hcWorksheetTemplate.setOemId(OEM.GM.getOem());
        hcWorksheetTemplate.setPrecision(3);
        hcWorksheetTemplate.setYear(2021);
        hcWorksheetTemplate.setVersion(2);
        hcWorksheetTemplate.setAdditionalInfo(Collections.emptyMap());
        return hcWorksheetTemplate;
    }

    private HCWorksheet getHCWorksheet1() {
        HCWorksheet hcWorksheet = new HCWorksheet();

        hcWorksheet.setCreatedByUserId("user1");
        hcWorksheet.setModifiedByUserId("user2");
        hcWorksheet.setDealerId("4");
        hcWorksheet.setDepartment("dep1");
        hcWorksheet.setOemId(OEM.GM.getOem());
        hcWorksheet.setVersion(2);
        hcWorksheet.setYear(2020);
        hcWorksheet.setPosition("s1");
        hcWorksheet.setId("id1");

        HCValue hcValue1 = new HCValue();
        hcValue1.setMonth(1);
        hcValue1.setValue(BigDecimal.ZERO);

        HCValue hcValue2 = new HCValue();
        hcValue2.setMonth(2);
        hcValue2.setValue(BigDecimal.ZERO);

        hcWorksheet.setValues(Stream.of(hcValue1,hcValue2).collect(Collectors.toList()));

        return hcWorksheet;
    }

    private HCWorksheet getHCWorksheet2() {
        HCWorksheet hcWorksheet = new HCWorksheet();
        hcWorksheet.setCreatedByUserId("user3");
        hcWorksheet.setModifiedByUserId("user4");
        hcWorksheet.setDealerId("4");
        hcWorksheet.setDepartment("dep2");
        hcWorksheet.setOemId(OEM.GM.getOem());
        hcWorksheet.setVersion(3);
        hcWorksheet.setYear(2019);
        hcWorksheet.setPosition("s2");
        hcWorksheet.setId("id2");

        HCValue hcValue1 = new HCValue();
        hcValue1.setMonth(1);
        hcValue1.setValue(BigDecimal.ZERO);

        HCValue hcValue2 = new HCValue();
        hcValue2.setMonth(2);
        hcValue2.setValue(BigDecimal.ZERO);

        hcWorksheet.setValues(Stream.of(hcValue1,hcValue2).collect(Collectors.toList()));

        return hcWorksheet;
    }

    private HCDepartment getHCDepartment(){
        HCDepartment hcDepartment = new HCDepartment();
        hcDepartment.setKey("dep1");
        hcDepartment.setName("name");
        hcDepartment.setOrder(2);
        hcDepartment.setSupportedPositions(Stream.of("s1","s2").collect(Collectors.toList()));
        return hcDepartment;
    }

    private HCBulkUpdateDto getHCBulkUpdateDto(){
        HCBulkUpdateDto hcBulkUpdateDto = new HCBulkUpdateDto();

        HCValue hcValue1 = new HCValue();
        hcValue1.setMonth(1);
        hcValue1.setValue(BigDecimal.ONE);

        HCValue hcValue2 = new HCValue();
        hcValue2.setMonth(2);
        hcValue2.setValue(BigDecimal.TEN);

        HCUpdateDto hcUpdateDto1 = new HCUpdateDto();
        hcUpdateDto1.setId("id1");
        hcUpdateDto1.setValues(Stream.of(hcValue1).collect(Collectors.toList()));

        HCUpdateDto hcUpdateDto2 = new HCUpdateDto();
        hcUpdateDto2.setId("id2");
        hcUpdateDto2.setValues(Stream.of(hcValue1,hcValue2).collect(Collectors.toList()));

        hcBulkUpdateDto.setHcWorksheets(Stream.of(hcUpdateDto1,hcUpdateDto2).collect(Collectors.toList()));

        return hcBulkUpdateDto;
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
        fsEntry.setFsType("INTERNAL");
        return fsEntry;
    }
}