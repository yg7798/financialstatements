package com.tekion.accounting.fs.service.oems;

import com.tekion.accounting.fs.beans.accountingInfo.AccountingInfo;
import com.tekion.accounting.fs.beans.common.AccountingOemFsCellCode;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.beans.common.OemConfig;
import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.accounting.fs.common.utils.TimeUtils;
import com.tekion.accounting.fs.dto.cellcode.FsCellCodeDetailsResponseDto;
import com.tekion.accounting.fs.dto.cellcode.FsCodeDetail;
import com.tekion.accounting.fs.dto.request.FinancialStatementRequestDto;
import com.tekion.accounting.fs.enums.FinancialYearType;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.service.accountingInfo.AccountingInfoService;
import com.tekion.accounting.fs.service.compute.FsComputeService;
import com.tekion.admin.beans.BrandMappingResponse;
import com.tekion.dealersettings.dealermaster.beans.DealerMaster;
import com.tekion.clients.preference.client.PreferenceClient;
import com.tekion.clients.preference.client.TekionResponse;
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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyBoolean;

@RunWith(MockitoJUnitRunner.class)
public class GMFinancialStatementServiceImplTest extends TestCase {
    @InjectMocks
    GMFinancialStatementServiceImpl gmFinancialStatementService;
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
    ServletOutputStream servletOutputStream;

    @Before
    public void setUp() {
        UserContextProvider.setContext(new UserContext("-1", "ca", "4"));
        Mockito.when(dealerConfig.getDealerTimeZone()).thenReturn(TimeZone.getDefault());
    }

    @Test
    public void testGenerateXML() {
        Mockito.when(preferenceClient.findBrandForMake(Mockito.any())).thenReturn(getTekionResponseList());
        Mockito.when(oemMappingService.getOemConfig(Mockito.anyString())).thenReturn(getOemConfig());
        Mockito.when(dealerConfig.getDealerMaster()).thenReturn(getDealerMaster());
        Mockito.when(oemMappingService.computeFsCellCodeDetails(any(FSEntry.class), anyLong(), anyBoolean(), anyBoolean())).thenReturn(getCellCodeDetails());
        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(Mockito.anyString(), Mockito.anyString())).thenReturn(getFsEntry());
        assertNotNull(gmFinancialStatementService.generateXML(getFinancialStatementRequestDto()));
    }

    @Test
    public void testDownloadFileForNormalYear() throws IOException {
        Mockito.doNothing().when(httpServletResponse).setHeader(Mockito.anyString(), Mockito.anyString());
        Mockito.doNothing().when(httpServletResponse).setContentType(Mockito.anyString());
        Mockito.when(httpServletResponse.getWriter()).thenReturn(getPrintWriter());
        Mockito.when(oemMappingService.computeFsCellCodeDetails(any(FSEntry.class), anyLong(), anyBoolean(), anyBoolean())).thenReturn(getCellCodeDetails());
        Mockito.when(preferenceClient.findBrandForMake(Mockito.any())).thenReturn(getTekionResponseList());
        Mockito.when(oemMappingService.getOemConfig(Mockito.anyString())).thenReturn(getOemConfig());
        Mockito.when(dealerConfig.getDealerMaster()).thenReturn(getDealerMaster());
        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(Mockito.anyString(), Mockito.anyString())).thenReturn(getFsEntry());
        Mockito.when(infoService.find(Mockito.anyString())).thenReturn(getAccountingInfo());
        gmFinancialStatementService.downloadFile(getFinancialStatementRequestDto(), httpServletResponse);
    }

    @Test
    public void testDownloadFileForNCB_STATEMENT_YEAR() throws IOException {
        Mockito.doNothing().when(httpServletResponse).setHeader(Mockito.anyString(), Mockito.anyString());
        Mockito.when(httpServletResponse.getOutputStream()).thenReturn(servletOutputStream);
        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(Mockito.anyString(), Mockito.anyString())).thenReturn(getFsEntry1());
        Mockito.when(oemMappingService.computeFsCellCodeDetails(any(FSEntry.class), anyLong(), anyBoolean(), anyBoolean())).thenReturn(getCellCodeDetails());
        Mockito.when(preferenceClient.findBrandForMake(Mockito.any())).thenReturn(getTekionResponseList());
        Mockito.when(oemMappingService.getOemConfig(Mockito.anyString())).thenReturn(getOemConfig());
        Mockito.when(dealerConfig.getDealerMaster()).thenReturn(getDealerMaster());
        Mockito.when(infoService.find(Mockito.anyString())).thenReturn(getAccountingInfo());
        gmFinancialStatementService.downloadFile(getFinancialStatementRequestDto(), httpServletResponse);
    }

    private PrintWriter getPrintWriter() {
        PrintWriter printWriter = new PrintWriter(System.out);
        return printWriter;
    }

    private AccountingInfo getAccountingInfo() {
        AccountingInfo accountingInfo = new AccountingInfo();
        accountingInfo.setId("1");
        accountingInfo.setPrimaryOEM(OEM.GM.getOem());
        accountingInfo.setSupportedOEMs(new HashSet<String>() {{
            add("Ford");
            add("FCA");
        }});
        accountingInfo.setBsdPresent(true);
        accountingInfo.setDealerId("5");
        return accountingInfo;
    }

    private TekionResponse<List<BrandMappingResponse>> getTekionResponseList() {
        BrandMappingResponse brandMappingResponse1 = new BrandMappingResponse();
        brandMappingResponse1.setBrand("xyz");
        brandMappingResponse1.setOem("Acura");

        BrandMappingResponse brandMappingResponse2 = new BrandMappingResponse();
        brandMappingResponse2.setBrand("abc");
        brandMappingResponse1.setOem("Acura");

        List<BrandMappingResponse> list = new ArrayList<>();
        list.add(brandMappingResponse1);
        list.add(brandMappingResponse2);
        return TekionResponse.<List<BrandMappingResponse>>builder().build().setData(list);
    }

    private OemConfig getOemConfig() {
        OemConfig oemConfig = new OemConfig();
        oemConfig.setOemId("Acura");
        oemConfig.setCountry("US");
        return oemConfig;
    }

    private DealerMaster getDealerMaster() {
        DealerMaster dealerMaster = new DealerMaster();
        dealerMaster.setTenantId("2");
        dealerMaster.setId("1234");
        return dealerMaster;
    }

    private FinancialStatementRequestDto getFinancialStatementRequestDto() {
        FinancialStatementRequestDto financialStatementRequestDto = new FinancialStatementRequestDto();
        financialStatementRequestDto.setFsId("6155a7d8b3cb1f0006868cd6");
        financialStatementRequestDto.setFinancialYearType(FinancialYearType.FISCAL_YEAR);
        return financialStatementRequestDto;
    }

    private FsCellCodeDetailsResponseDto getCellCodeDetails() {
        FsCellCodeDetailsResponseDto fsCellCodeDetailsResponseDto = new FsCellCodeDetailsResponseDto();
        fsCellCodeDetailsResponseDto.setAccountingOemFsCellCodes(getFsCellCodesList());
        fsCellCodeDetailsResponseDto.setCodeVsDetailsMap(getDetailsMap());
        return fsCellCodeDetailsResponseDto;
    }

    private List<AccountingOemFsCellCode> getFsCellCodesList() {
        AccountingOemFsCellCode accountingOemFsCellCode1 = new AccountingOemFsCellCode();
        accountingOemFsCellCode1.setOemCode("1-3");
        accountingOemFsCellCode1.setOemId("Acura");
        accountingOemFsCellCode1.setCode("1");
        accountingOemFsCellCode1.setCountry("US");

        AccountingOemFsCellCode accountingOemFsCellCode2 = new AccountingOemFsCellCode();
        accountingOemFsCellCode2.setOemCode("4-6");
        accountingOemFsCellCode2.setOemId("Audi");
        accountingOemFsCellCode2.setCode("2");
        accountingOemFsCellCode2.setCountry("US");

        List<AccountingOemFsCellCode> list = new ArrayList<>();
        list.add(accountingOemFsCellCode1);
        list.add(accountingOemFsCellCode2);
        return list;
    }

    private Map<String, FsCodeDetail> getDetailsMap() {
        FsCodeDetail fsCodeDetail1 = new FsCodeDetail();
        fsCodeDetail1.setStringValue("1");
        fsCodeDetail1.setValue(new BigDecimal(4));

        FsCodeDetail fsCodeDetail2 = new FsCodeDetail();
        fsCodeDetail2.setStringValue("2");
        fsCodeDetail2.setValue(new BigDecimal(5));

        Map<String, FsCodeDetail> map = new HashMap<>();
        map.put("1", fsCodeDetail1);
        map.put("2", fsCodeDetail2);
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

    private FSEntry getFsEntry1() {
        FSEntry fsEntry = new FSEntry();
        fsEntry.setDealerId("5");
        fsEntry.setYear(0);
        fsEntry.setVersion(1);
        fsEntry.setOemId("Acura");
        fsEntry.setId("6155a7d8b3cb1f0006868cd6");
        fsEntry.setSiteId("-1_5");
        fsEntry.setFsType("INTERNAL");
        return fsEntry;
    }
}
