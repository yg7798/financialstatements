package com.tekion.accounting.service.worksheet;

import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.beans.memo.HCDepartment;
import com.tekion.accounting.fs.beans.memo.HCValue;
import com.tekion.accounting.fs.beans.memo.HCWorksheet;
import com.tekion.accounting.fs.beans.memo.HCWorksheetTemplate;
import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.accounting.fs.common.utils.UserContextUtils;
import com.tekion.accounting.fs.dto.memo.HCBulkUpdateDto;
import com.tekion.accounting.fs.dto.memo.HCUpdateDto;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.repos.worksheet.HCWorksheetRepo;
import com.tekion.accounting.fs.repos.worksheet.HCWorksheetTemplateRepo;
import com.tekion.accounting.fs.service.worksheet.HCWorksheetService;
import com.tekion.accounting.fs.service.worksheet.HCWorksheetServiceImpl;
import com.tekion.core.utils.UserContext;
import com.tekion.core.utils.UserContextProvider;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@PrepareForTest(UserContextUtils.class)
@RunWith(MockitoJUnitRunner.Silent.class)
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
        Mockito.when(hcWorksheetRepo.findByIds(Mockito.anyList(), Mockito.anyString(),Mockito.anyString())).
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
        hcDepartment.setKey("key");
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