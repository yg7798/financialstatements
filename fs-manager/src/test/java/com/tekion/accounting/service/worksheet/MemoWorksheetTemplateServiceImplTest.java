package com.tekion.accounting.service.worksheet;

import com.tekion.accounting.fs.beans.memo.FieldType;
import com.tekion.accounting.fs.beans.memo.MemoWorksheetTemplate;
import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.accounting.fs.dto.memo.MemoWorksheetTemplateRequestDto;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.repos.worksheet.MemoWorksheetTemplateRepo;
import com.tekion.accounting.fs.service.worksheet.MemoWorksheetTemplateServiceImpl;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.event.annotation.AfterTestMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(MockitoJUnitRunner.Silent.class)
public class MemoWorksheetTemplateServiceImplTest extends TestCase {
    @Mock
    MemoWorksheetTemplateRepo memoWorksheetTemplateRepo;
    @Mock
    DealerConfig dealerConfig;
    @InjectMocks
    MemoWorksheetTemplateServiceImpl memoWorksheetService;

    @Before
    public void setUp(){
        Mockito.when(dealerConfig.getDealerCountryCode()).thenReturn("US");
    }

    @AfterTestMethod
    public void cleanUp() {
        Mockito.reset(memoWorksheetTemplateRepo);
    }

    @Test
    public void testGetMemoWorksheetTemplates() {
        Mockito.when(memoWorksheetTemplateRepo.findByOemYearAndCountry(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(getMemoWorksheetTemplateList());
        assertEquals(2, memoWorksheetService.getMemoWorksheetTemplates(OEM.GM, 2021, 1).size());
        Mockito.verify(memoWorksheetTemplateRepo, Mockito.times(1)).findByOemYearAndCountry(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString());
    }

    @Test
    public void testSave() {
        MemoWorksheetTemplateRequestDto requestDto = getMemoWorksheetTemplateReqDtoList().get(0);
        Mockito.when(memoWorksheetTemplateRepo.save(Mockito.any())).thenReturn(new MemoWorksheetTemplate());
        memoWorksheetService.save(requestDto);
        Mockito.verify(memoWorksheetTemplateRepo, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    public void testSaveBulk() {
        Mockito.when(memoWorksheetTemplateRepo.updateBulk(Mockito.any())).thenReturn(new ArrayList<>());
        memoWorksheetService.saveBulk(getMemoWorksheetTemplateReqDtoList());
        Mockito.verify(memoWorksheetTemplateRepo, Mockito.times(1)).updateBulk(Mockito.any());
    }

    @Test
    public void testDeleteMemoWorksheetTemplatesByKeys() {
        Mockito.when(memoWorksheetTemplateRepo.findByOemYearAndCountry(Mockito.anyString(), Mockito.anyInt(),Mockito.anyInt(),Mockito.any(),Mockito.anyString())).thenReturn(getMemoWorksheetTemplateList());
        Mockito.doNothing().when(memoWorksheetTemplateRepo).deleteTemplatesByKey(Mockito.anyString(), Mockito.anyInt(),Mockito.anyInt(),Mockito.any(),Mockito.anyString());
        assertEquals(2, memoWorksheetService.deleteMemoWorksheetTemplatesByKeys(OEM.GM, 2021, 1, Arrays.asList("fsPage").stream().collect(Collectors.toSet()), "US").size());
        Mockito.reset(memoWorksheetTemplateRepo);
    }


    private List<MemoWorksheetTemplate> getMemoWorksheetTemplateList() {
        List<MemoWorksheetTemplate> memoWorksheetTemplateList = new ArrayList<>();
        MemoWorksheetTemplate memoWorksheetTemplate1 = MemoWorksheetTemplate.builder()
                .country("US")
                .key("fsPage")
                .oemId("GM")
                .year(2020)
                .name("FS Page")
                .version(1)
                .build();
        MemoWorksheetTemplate memoWorksheetTemplate2 = MemoWorksheetTemplate.builder()
                .country("US")
                .key("GM")
                .year(2020)
                .oemId("fsLine")
                .name("FS line")
                .version(1)
                .build();
        memoWorksheetTemplateList.add(memoWorksheetTemplate1);
        memoWorksheetTemplateList.add(memoWorksheetTemplate2);
        return memoWorksheetTemplateList;
    }

    private List<MemoWorksheetTemplateRequestDto> getMemoWorksheetTemplateReqDtoList(){
        List<MemoWorksheetTemplateRequestDto> dtoList = new ArrayList<>();
        MemoWorksheetTemplateRequestDto requestDto1 = new MemoWorksheetTemplateRequestDto();
        requestDto1.setName("MemoWorksheet");
        requestDto1.setLineNumber("1");
        requestDto1.setPageNumber("1");
        requestDto1.setYear(2021);
        requestDto1.setOem(OEM.GM);
        requestDto1.setFieldType(FieldType.COUNT);
        dtoList.add(requestDto1);

        MemoWorksheetTemplateRequestDto requestDto2 = new MemoWorksheetTemplateRequestDto();
        requestDto2.setName("MemoWorksheet");
        requestDto2.setLineNumber("2");
        requestDto2.setPageNumber("1");
        requestDto2.setYear(2021);
        requestDto2.setOem(OEM.GM);
        requestDto2.setFieldType(FieldType.COUNT);
        dtoList.add(requestDto2);
        return  dtoList;
    }
}
