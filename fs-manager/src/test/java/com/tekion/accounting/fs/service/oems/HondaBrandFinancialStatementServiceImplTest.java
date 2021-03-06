package com.tekion.accounting.fs.service.oems;

import com.tekion.accounting.commons.dealer.DealerConfig;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.beans.common.OemConfig;
import com.tekion.accounting.fs.common.utils.TimeUtils;
import com.tekion.accounting.fs.dto.cellcode.FsCellCodeDetailsResponseDto;
import com.tekion.accounting.fs.dto.request.FinancialStatementRequestDto;
import com.tekion.accounting.fs.enums.FinancialYearType;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.service.accountingInfo.AccountingInfoService;
import com.tekion.accounting.fs.service.compute.FsComputeService;
import com.tekion.accounting.fs.service.oemConfig.OemConfigService;
import com.tekion.admin.beans.BrandMappingResponse;
import com.tekion.dealersettings.dealermaster.beans.DealerMaster;
import com.tekion.clients.preference.client.PreferenceClient;
import com.tekion.clients.preference.client.TekionResponse;
import com.tekion.core.exceptions.TBaseRuntimeException;
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
import org.springframework.test.context.event.annotation.AfterTestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyBoolean;

@RunWith(MockitoJUnitRunner.class)
public class HondaBrandFinancialStatementServiceImplTest extends TestCase {
    @InjectMocks
    HondaBrandFinancialStatementServiceImpl hondaBrandFinancialStatementService;
    @InjectMocks
    TimeUtils timeUtils;

    @Mock
    DealerConfig dealerConfig;
    @Mock
    FSEntryRepo fsEntryRepo;
    @Mock
    AbstractFinancialStatementService financialStatementService;
    @Mock
    FsComputeService oemMappingService;
    @Mock
    PreferenceClient preferenceClient;
    @Mock
    IntegrationService integrationService;
    @Mock
    AccountingInfoService infoService;
    @Mock
    HttpServletResponse httpServletResponse;
    @Mock
    OemConfigService oemConfigService;

    @Before
    public void setUp() {
        UserContextProvider.setContext(new UserContext("-1", "ca", "4"));
        Mockito.when(dealerConfig.getDealerTimeZone()).thenReturn(TimeZone.getDefault());
        Mockito.when(dealerConfig.getDealerTimeZoneName()).thenReturn("123");
    }

    @AfterTestMethod
    public void cleanUp() {
        Mockito.reset(fsEntryRepo);
    }

    @Test(expected = TBaseRuntimeException.class)
    public void testGenerateXML() {
        assertNotNull( hondaBrandFinancialStatementService.generateXML(getFinancialStatementRequestDto()));
    }

    @Test
    public void testDownloadFile()  throws IOException{
        Mockito.doNothing().when(httpServletResponse).setHeader(Mockito.anyString(),Mockito.anyString());
        Mockito.doNothing().when(httpServletResponse).setContentType(Mockito.anyString());
        Mockito.when(httpServletResponse.getWriter()).thenReturn(getPrintWriter());
        Mockito.when(oemMappingService.computeFsCellCodeDetails(any(FSEntry.class), anyLong(), anyBoolean(), anyBoolean())).thenReturn(getCellCodeDetails());
        Mockito.when(preferenceClient.findBrandForMake(Mockito.any())).thenReturn(getTekionResponseList());
        Mockito.when(oemConfigService.getOemConfig(Mockito.anyString())).thenReturn(getOemConfig());
        Mockito.when(dealerConfig.getDealerMaster()).thenReturn(getDealerMaster());
        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(Mockito.anyString(), Mockito.anyString())).thenReturn(getFsEntry());
        hondaBrandFinancialStatementService.downloadFile(getFinancialStatementRequestDto(), httpServletResponse);
    }

    private PrintWriter getPrintWriter() {
        PrintWriter printWriter=new PrintWriter(System.out);
        return printWriter;
    }

    private FsCellCodeDetailsResponseDto getCellCodeDetails(){
        FsCellCodeDetailsResponseDto fsCellCodeDetailsResponseDto=new FsCellCodeDetailsResponseDto();
        fsCellCodeDetailsResponseDto.setAccountingOemFsCellCodes(new ArrayList<>());
        fsCellCodeDetailsResponseDto.setCodeVsDetailsMap(new HashMap<>());
        return fsCellCodeDetailsResponseDto;
    }

    private FinancialStatementRequestDto getFinancialStatementRequestDto() {
        FinancialStatementRequestDto financialStatementRequestDto = new FinancialStatementRequestDto();
        financialStatementRequestDto.setFsId("1234");
        financialStatementRequestDto.setFinancialYearType(FinancialYearType.FISCAL_YEAR);
        return financialStatementRequestDto;
    }

    private TekionResponse<List<BrandMappingResponse>> getTekionResponseList(){
        BrandMappingResponse brandMappingResponse1=new BrandMappingResponse();
        brandMappingResponse1.setBrand("xyz");
        brandMappingResponse1.setOem("Acura");

        BrandMappingResponse brandMappingResponse2=new BrandMappingResponse();
        brandMappingResponse2.setBrand("abc");
        brandMappingResponse1.setOem("Acura");

        List<BrandMappingResponse> list=new ArrayList<>();
        list.add(brandMappingResponse1);
        list.add(brandMappingResponse2);
        return TekionResponse.<List<BrandMappingResponse>>builder().build().setData(list);
    }
    private OemConfig getOemConfig() {
        OemConfig oemConfig=new OemConfig();
        oemConfig.setOemId("Acura");
        oemConfig.setCountry("US");
        return oemConfig;
    }

    private DealerMaster getDealerMaster() {
        DealerMaster dealerMaster=new DealerMaster();
        dealerMaster.setTenantId("2");
        dealerMaster.setId("1234");
        return dealerMaster;
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
