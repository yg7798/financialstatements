package com.tekion.accounting.fs.service.oems;

import com.google.common.collect.Lists;
import com.tekion.accounting.commons.dealer.DealerConfig;
import com.tekion.accounting.fs.beans.common.AccountingOemFsCellCode;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.beans.common.OemConfig;
import com.tekion.accounting.fs.common.GlobalService;
import com.tekion.accounting.fs.common.utils.TimeUtils;
import com.tekion.accounting.fs.dto.cellcode.FsCellCodeDetailsResponseDto;
import com.tekion.accounting.fs.dto.cellcode.FsCodeDetail;
import com.tekion.accounting.fs.dto.integration.FSIntegrationRequest;
import com.tekion.accounting.fs.dto.mappings.OemSiteDetailsDto;
import com.tekion.accounting.fs.dto.request.FinancialStatementRequestDto;
import com.tekion.accounting.fs.enums.FinancialYearType;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.service.accountingInfo.AccountingInfoService;
import com.tekion.accounting.fs.service.compute.FsComputeService;
import com.tekion.accounting.fs.service.integration.IntegrationClient;
import com.tekion.admin.beans.BrandMappingResponse;
import com.tekion.admin.beans.FindBrandRequest;
import com.tekion.dealersettings.dealermaster.beans.DealerMaster;
import com.tekion.client.globalsettings.beans.dto.DealerInfoWithOEMDetails;
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
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultFSServiceImplTest extends TestCase {

    @InjectMocks
    DefaultFSServiceImpl defaultFSService;
    @InjectMocks
    TimeUtils timeUtils;

    @Mock
    DealerConfig dealerConfig;
    @Mock
    AbstractFinancialStatementService financialStatementService;
    @Mock
    FsComputeService oemMappingService;
    @Mock
    IntegrationClient integrationClient;
    @Mock
    FSEntryRepo fsEntryRepo;
    @Mock
    PreferenceClient preferenceClient;
    @Mock
    GlobalService globalService;
    @Mock
    IntegrationService integrationService;
    @Mock
    AccountingInfoService accountingInfoService;

    @Before
    public void setUp() {
        UserContextProvider.setContext(new UserContext("-1", "ca", "4"));
        when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(anyString(), anyString())).thenReturn(getFsEntry());
        when(dealerConfig.getDealerTimeZone()).thenReturn(TimeZone.getTimeZone("America/Los_Angeles"));
    }

    @Test(expected = TBaseRuntimeException.class)
    public void testGenerateXML() {
        assertNotNull(defaultFSService.generateXML(getFinancialStatementRequestDto()));
    }

    @Test
    public void testSubmit(){
        when(dealerConfig.getDealerMaster()).thenReturn(getDealerMaster());
        when(oemMappingService.getOemConfig(anyString())).thenReturn(getOemConfig("GM"));
        when(preferenceClient.findBrandForMake(any(FindBrandRequest.class))).thenReturn(getBrandMappingResponse());
        when(oemMappingService.computeFsCellCodeDetails(any(FSEntry.class), anyLong(), anyBoolean(), anyBoolean()))
                .thenReturn(getCellCodeDetails());
        defaultFSService.submit(getFSRequestDto());
    }

    @Test(expected = TBaseRuntimeException.class)
    public void testException(){
        when(dealerConfig.getDealerMaster()).thenReturn(getDealerMaster());
        when(oemMappingService.getOemConfig(anyString())).thenReturn(getOemConfig("GM"));
        when(preferenceClient.findBrandForMake(any(FindBrandRequest.class))).thenReturn(getBrandMappingResponse());
        when(oemMappingService.computeFsCellCodeDetails(any(FSEntry.class), anyLong(), anyBoolean(), anyBoolean())).thenReturn(getCellCodeDetails());
        when(globalService.getAllDealerDetailsForTenant(anyString())).thenReturn(new ArrayList<>());
        when(integrationClient.submitFS(any(FSIntegrationRequest.class), any(OEMInfo.class), any())).thenThrow(TBaseRuntimeException.class);
        DealerMaster dm = DealerMaster.builder().id("gsfgs").dealerName("bhshs").build();
        when(globalService.getAllDealerDetailsForTenant(anyString())).thenReturn(Collections.singletonList(dm));
        OemSiteDetailsDto dto = new OemSiteDetailsDto();
        dto.setName("");
        dto.setSiteId("");
        when(globalService.getOemSiteDetails()).thenReturn(Collections.singletonList(dto));
        defaultFSService.submit(getFSRequestDto());
    }

    private FinancialStatementRequestDto getFinancialStatementRequestDto() {
        FinancialStatementRequestDto financialStatementRequestDto = new FinancialStatementRequestDto();
        financialStatementRequestDto.setFsId("1234");
        financialStatementRequestDto.setFinancialYearType(FinancialYearType.FISCAL_YEAR);
        return financialStatementRequestDto;
    }

     FSEntry getFsEntry() {
        return FSEntry.builder()
                .fsType("OEM").dealerId("1")
                .oemId("GM").year(2021).build();
    }

    FsCellCodeDetailsResponseDto getCellCodeDetails(){
        FsCellCodeDetailsResponseDto dto = new FsCellCodeDetailsResponseDto();
        dto.setAccountingOemFsCellCodes(Collections.singletonList(getCellCode("c1", "c1", "")));
        dto.setCodeVsDetailsMap(getCodeVsDetailsMap());
        return dto;
    }

    FsCodeDetail getFsCodeDetail(){
        return FsCodeDetail.builder().value(BigDecimal.ZERO).stringValue("0").build();
    }

    AccountingOemFsCellCode getCellCode(String code, String oemCode, String durationType){
        return AccountingOemFsCellCode.builder().code(code).durationType(durationType)
                .additionalInfo(new HashMap<>()).oemCode(oemCode).build();
    }

    OemConfig getOemConfig(String oem){
        return OemConfig.builder().oemId(oem).build();
    }

    Map<String, FsCodeDetail> getCodeVsDetailsMap(){
        Map<String, FsCodeDetail> map = new HashMap<>();
        map.put("c1", getFsCodeDetail());
        return map;
    }

    FinancialStatementRequestDto getFSRequestDto(){
        return FinancialStatementRequestDto.builder().fsId("1").addM13BalInDecBalances(false)
                .financialYearType(FinancialYearType.CALENDAR_YEAR).tillEpoch(123).build();
    }

    DealerMaster getDealerMaster() {
        return DealerMaster.builder().oemDealerId("1").build();
    }

    TekionResponse<List<BrandMappingResponse>> getBrandMappingResponse(){
        BrandMappingResponse brandMappingResponse = new BrandMappingResponse();
        brandMappingResponse.setActualBrand("gm");
        return new TekionResponse<>(Collections.singletonList(brandMappingResponse), "success");
    }

    public static List<DealerInfoWithOEMDetails> getSingleDealerInfoList(){
        DealerInfoWithOEMDetails exampleDealerInfo = getExampleDealerInfo();
        return Lists.newArrayList(exampleDealerInfo);
    }

    public static DealerInfoWithOEMDetails getExampleDealerInfo(){
        DealerInfoWithOEMDetails dealerInfoWithOEMDetails = new DealerInfoWithOEMDetails();
        dealerInfoWithOEMDetails.setDealerId("4");
        dealerInfoWithOEMDetails.setTenantId("cacargroup");
        return dealerInfoWithOEMDetails;
    }
}
