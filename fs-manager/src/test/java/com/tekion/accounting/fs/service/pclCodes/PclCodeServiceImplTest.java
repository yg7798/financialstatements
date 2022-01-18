package com.tekion.accounting.fs.service.pclCodes;

import com.google.common.collect.Maps;
import com.tekion.accounting.fs.beans.common.AccountingOemFsCellGroup;
import com.tekion.accounting.fs.beans.common.OemTemplate;
import com.tekion.accounting.fs.dto.pclCodes.MediaRequestDto;
import com.tekion.accounting.fs.repos.OemFsCellGroupRepo;
import com.tekion.accounting.fs.repos.OemTemplateRepo;
import com.tekion.accounting.fs.service.common.FileCommons;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.utils.TCollectionUtils;
import junit.framework.TestCase;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class PclCodeServiceImplTest extends TestCase {

    @Mock
    OemTemplateRepo oemTemplateRepo;
    @Mock
    OemFsCellGroupRepo oemFsCellGroupRepo;
    @InjectMocks
    PclCodeServiceImpl pclCodeService;
    @Mock
    FileCommons fileCommons;
    @Rule
    public TemporaryFolder folder= new TemporaryFolder();

    @Test
    public void testGetOemDetails() {
        Mockito.when(oemTemplateRepo.findAllOemDetails())
                .thenReturn(getOemDetails());
        assertEquals(2, pclCodeService.getOemDetails().size());
    }

    @Test
    public void testGetPclCodeDetails() {
        Mockito.when(oemFsCellGroupRepo.findByOemId(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(getAccountingOemFsCellGroupList());
        assertEquals(1, pclCodeService.getPclCodeDetails("GM", 2021, "US").size());
        Mockito.verify(oemFsCellGroupRepo, Mockito.times(1))
                .findByOemId(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString());
    }

    @Test
    public void testUpdatePclCodeDetails_success() {
        Mockito.when(oemFsCellGroupRepo
                .findByGroupCode(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(getAccountingOemFsCellGroupList().get(0));
        pclCodeService.updatePclCodeDetails(getPclDetailsDto());

        ArgumentCaptor<AccountingOemFsCellGroup> captor = ArgumentCaptor.forClass(AccountingOemFsCellGroup.class);
        Mockito.verify(oemFsCellGroupRepo, Mockito.times(1)).save(captor.capture());
        AccountingOemFsCellGroup accountingOemFsCellGroup = captor.getValue();
        assertEquals("_202", accountingOemFsCellGroup.getGroupCode());
        assertEquals("402A", accountingOemFsCellGroup.getAutomatePcl());
        assertEquals("1108A", accountingOemFsCellGroup.getAutosoftPcl());
        Mockito.verify(oemFsCellGroupRepo, Mockito.times(1))
                .findByGroupCode(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString());
    }

    @Test(expected = TBaseRuntimeException.class)
    public void testUpdatePclCodeDetails_Failure() {
        Mockito.when(oemFsCellGroupRepo
                        .findByGroupCode(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(null);
        pclCodeService.updatePclCodeDetails(getPclDetailsDto());
    }

    @Test
    public void testUpdatePclCodeBulk_success() throws IOException {
        Mockito.when(fileCommons.downloadFileUsingMediaId(Mockito.anyString()))
                .thenReturn(getXLSXFile());
        Mockito.when(oemFsCellGroupRepo.findByGroupCode(Mockito.anySet(), Mockito.anySet(), Mockito.anySet(), Mockito.anySet()))
                .thenReturn(getAccountingOemFsCellGroupList());
        MediaRequestDto mediaRequestDto = new MediaRequestDto();
        mediaRequestDto.setMediaId("mediaId");
        pclCodeService.updatePclCodesInBulk(mediaRequestDto);
        Mockito.verify(oemFsCellGroupRepo, Mockito.times(1)).findByGroupCode(Mockito.anySet(), Mockito.anySet(), Mockito.anySet(), Mockito.anySet());
    }

    @Test(
            expected = TBaseRuntimeException.class
    )
    public void testUpdatePclCodeBulk_Failure() throws IOException {
        Mockito.when(fileCommons.downloadFileUsingMediaId(Mockito.anyString()))
                .thenReturn(null);
        MediaRequestDto mediaRequestDto = new MediaRequestDto();
        mediaRequestDto.setMediaId("mediaId");
        pclCodeService.updatePclCodesInBulk(mediaRequestDto);
    }

    public static Workbook createExcel(Workbook workbook, List<String> headers, Map<String, List<String>> rowInformation){
        workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("testing");
        Row header = sheet.createRow(0);
        int i=0 ;
        for(String headerName : headers) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headerName);
            i++;
        }
        int rowCount = 1;
        for(Map.Entry<String, List<String>> entry : TCollectionUtils.nullSafeMap(rowInformation).entrySet()){
            Row row = sheet.createRow(rowCount);
            rowCount++;
            i=0;
            if(entry.getValue() != null){
                for (String rowName : entry.getValue()) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue(rowName);
                    i++;
                }
            }
        }
        return workbook;
    }

    private File getXLSXFile() throws IOException {
        Workbook workbook = null;
        File file = folder.newFile("testing.xlsx");
        FileOutputStream outputStream = new FileOutputStream(file);
        List<String> headers = Arrays.asList("h1", "h2", "h3", "h4", "h5","h6", "h7", "h8", "h9", "h10", "h11", "h12", "h13", "h14");
        Map<String, List<String>> rows = Maps.newHashMap();
        rows.put("1", Arrays.asList("GM", "2021", "US", "202", "_202","v6", "v7", "v8", "v9", "v10", "v11", "v12", "v13", "v14"));
        workbook = PclCodeServiceImplTest.createExcel(workbook, headers, rows);
        workbook.write(outputStream);
        workbook.close();
        return file;
    }


    private List<OemTemplate> getOemDetails() {
        List<OemTemplate> oemDetails = new ArrayList<>();
        OemTemplate oemDetails1 = new OemTemplate();
        oemDetails1.setOemId("GM");
        oemDetails1.setCountry("US");
        oemDetails1.setYear(2021);

        OemTemplate oemDetails2 = new OemTemplate();
        oemDetails2.setOemId("FCA");
        oemDetails2.setCountry("CA");
        oemDetails2.setYear(2021);
        oemDetails.add(oemDetails1);
        oemDetails.add(oemDetails2);
        return oemDetails;
    }

    private List<AccountingOemFsCellGroup> getAccountingOemFsCellGroupList() {
        AccountingOemFsCellGroup accountingOemFsCellGroup = AccountingOemFsCellGroup.builder()
                .oemId("GM")
                .year(2021)
                .country("US")
                .groupCode("_202")
                .automatePcl("202A")
                .autosoftPcl("0108A")
                .cdkPcl("1A08")
                .groupDisplayName("202")
                .build();
        return Arrays.asList(accountingOemFsCellGroup);
    }

    private AccountingOemFsCellGroup getPclDetailsDto() {
        AccountingOemFsCellGroup pclDetailsDto = AccountingOemFsCellGroup.builder()
                .oemId("GM")
                .year(2021)
                .groupCode("_202")
                .automatePcl("402A")
                .autosoftPcl("1108A")
                .cdkPcl("1A08")
                .groupDisplayName("202")
                .country("US")
                .build();
        return pclDetailsDto;
    }
}
