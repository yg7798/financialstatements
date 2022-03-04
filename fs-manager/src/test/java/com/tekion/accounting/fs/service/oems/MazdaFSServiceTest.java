package com.tekion.accounting.fs.service.oems;

import com.tekion.accounting.fs.beans.common.*;
import com.tekion.accounting.fs.common.GlobalService;
import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.accounting.fs.common.utils.TimeUtils;
import com.tekion.accounting.fs.dto.cellcode.FsCellCodeDetailsResponseDto;
import com.tekion.accounting.fs.dto.integration.FSSubmitResponse;
import com.tekion.accounting.fs.dto.request.FinancialStatementRequestDto;
import com.tekion.accounting.fs.enums.FinancialYearType;
import com.tekion.accounting.fs.integration.Detail;
import com.tekion.accounting.fs.integration.ProcessFinancialStatement;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.service.accountingInfo.AccountingInfoService;
import com.tekion.accounting.fs.service.compute.FsComputeService;
import com.tekion.accounting.fs.service.external.nct.FillDetailContext;
import com.tekion.accounting.fs.service.fsMetaData.OemFSMetadataMappingService;
import com.tekion.accounting.fs.service.integration.IntegrationClient;
import com.tekion.accounting.fs.service.integration.IntegrationInternal;
import com.tekion.admin.beans.BrandMappingResponse;
import com.tekion.dealersettings.dealermaster.beans.DealerMaster;
import com.tekion.clients.preference.client.PreferenceClient;
import com.tekion.clients.preference.client.TekionResponse;
import com.tekion.core.excelGeneration.models.model.template.SingleCellData;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyBoolean;

@RunWith(MockitoJUnitRunner.class)
public class MazdaFSServiceTest extends TestCase {

    @InjectMocks
    MazdaFSService mazdaFSService;
    @InjectMocks
    TimeUtils timeUtils;

    @Mock
    DealerConfig dealerConfig;
    @Mock
    AbstractFinancialStatementService financialStatementService;
    @Mock
    FSEntryRepo fsEntryRepo;
    @Mock
    FsComputeService oemMappingService;
    @Mock
    PreferenceClient preferenceClient;
    @Mock
    IntegrationService integrationService;
    @Mock
    AccountingInfoService infoService;
    @Mock
    GlobalService globalService;
    @Mock
    IntegrationClient integrationClient;
    @Mock
    IntegrationInternal integrationInternal;
    @Mock
    OemFSMetadataMappingService fsMetadataMappingService;

    @Before
    public void setUp() {
        UserContextProvider.setContext(new UserContext("-1", "ca", "4"));
        Mockito.when(dealerConfig.getDealerTimeZone()).thenReturn(TimeZone.getDefault());
        Mockito.when(dealerConfig.getDealerMaster()).thenReturn(getDealerMaster());
    }

    @Test(expected = TBaseRuntimeException.class)
    public void testSetValueInDetail() {
        mazdaFSService.setValueInDetail(getFillDetailContextValue());
        Mockito.verify(mazdaFSService, Mockito.times(1)).setValueInDetail(getFillDetailContextValue());
    }

    @Test
    public void testSubmit() {
        Mockito.when(oemMappingService.computeFsCellCodeDetails(any(FSEntry.class), anyLong(), anyBoolean(), anyBoolean())).thenReturn(getCellCodeDetails());
        Mockito.when(oemMappingService.getOemConfig(Mockito.anyString())).thenReturn(getOemConfig());
        Mockito.when(preferenceClient.findBrandForMake(Mockito.any())).thenReturn(getTekionResponseList());
        Mockito.when(integrationClient.submitFS(Mockito.any(), Mockito.any(), Mockito.anyString())).thenReturn(getFSSubmitResponse());
        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(Mockito.anyString(), Mockito.anyString())).thenReturn(getFsEntry());
        assertEquals(getFSSubmitResponse(), mazdaFSService.submit(getFinancialStatementRequestDto()));

    }

    @Test(expected = TBaseRuntimeException.class)
    public void testGenerateXML() {
        assertEquals("xyz", mazdaFSService.generateXML(getFinancialStatementRequestDto()));
    }

    @Test
    public void testGetCellLevelFSReportData() {
        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(Mockito.anyString(), Mockito.anyString())).thenReturn(getFsEntry());
        assertNotNull(mazdaFSService.getCellLevelFSReportData(getFinancialStatementRequestDto()));
    }

    @Test
    public void testGetStatement() {
        Mockito.when(oemMappingService.computeFsCellCodeDetails(any(FSEntry.class), anyLong(), anyBoolean(), anyBoolean())).thenReturn(getCellCodeDetails());
        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(Mockito.anyString(), Mockito.anyString())).thenReturn(getFsEntry());
        Mockito.when(oemMappingService.getOemConfig(Mockito.anyString())).thenReturn(getOemConfig());
        Mockito.when(preferenceClient.findBrandForMake(Mockito.any())).thenReturn(getTekionResponseList());
        assertNotNull(mazdaFSService.getStatement(getFinancialStatementRequestDto()));
    }

    private List<AccountingOemFsCellCode> getAccountingOemFsCellGroupList() {
        List<AccountingOemFsCellCode> list = new ArrayList<>();
        AccountingOemFsCellCode accountingOemFsCellCode = new AccountingOemFsCellCode();
        accountingOemFsCellCode.setOemId("Acura");
        accountingOemFsCellCode.setCountry("US");
        accountingOemFsCellCode.setGroupCode("123");
        accountingOemFsCellCode.setVersion(1);
        accountingOemFsCellCode.setYear(2022);
        list.add(accountingOemFsCellCode);
        return list;
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

    private FSSubmitResponse getFSSubmitResponse() {
        FSSubmitResponse fsSubmitResponse = new FSSubmitResponse();
        fsSubmitResponse.setStatus("true");
        return fsSubmitResponse;
    }

    private DealerMaster getDealerMaster() {
        DealerMaster dealerMaster = new DealerMaster();
        dealerMaster.setTenantId("2");
        dealerMaster.setId("1234");
        return dealerMaster;
    }

    private FsCellCodeDetailsResponseDto getCellCodeDetails() {
        FsCellCodeDetailsResponseDto fsCellCodeDetailsResponseDto = new FsCellCodeDetailsResponseDto();
        fsCellCodeDetailsResponseDto.setAccountingOemFsCellCodes(new ArrayList<>());
        fsCellCodeDetailsResponseDto.setCodeVsDetailsMap(new HashMap<>());
        return fsCellCodeDetailsResponseDto;
    }

    private FillDetailContext getFillDetailContextValue() {
        FillDetailContext fillDetailContext = new FillDetailContext();
        Detail detail = new Detail();
        detail.setDescription("succeed");
        detail.setBalance1("123");
        detail.setBalance2("456");
        detail.setUnit1("a");
        detail.setAccountId("1234");
        fillDetailContext.setDetail(detail);
        fillDetailContext.setValueString("123");
        fillDetailContext.setValue(new BigDecimal(5));
        fillDetailContext.setOemConfig(getOemConfig());
        fillDetailContext.setCellCode(getCellCode());
        return fillDetailContext;
    }

    private AccountingOemFsCellCode getCellCode() {
        AccountingOemFsCellCode accountingOemFsCellCode = new AccountingOemFsCellCode();
        accountingOemFsCellCode.setCode("K1");
        accountingOemFsCellCode.setOemCode("K2");
        accountingOemFsCellCode.setOemId("Acura");
        accountingOemFsCellCode.setDisplayName("_K1");
        accountingOemFsCellCode.setCountry("us");
        return accountingOemFsCellCode;
    }

    private OemConfig getOemConfig() {
        OemConfig oemConfig = new OemConfig();
        oemConfig.setOemId("Acura");
        oemConfig.setCountry("US");
        return oemConfig;
    }

    private FinancialStatementRequestDto getFinancialStatementRequestDto() {
        FinancialStatementRequestDto financialStatementRequestDto = new FinancialStatementRequestDto();
        financialStatementRequestDto.setFsId("6155a7d8b3cb1f0006868cd6");
        financialStatementRequestDto.setFinancialYearType(FinancialYearType.FISCAL_YEAR);
        return financialStatementRequestDto;
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

    private List<SingleCellData> getSingleCellDataList() {
        SingleCellData singleCellData1 = new SingleCellData();
        singleCellData1.setAddress("abc");

        SingleCellData singleCellData2 = new SingleCellData();
        singleCellData2.setAddress("xyz");

        List<SingleCellData> list = new ArrayList<>();
        list.add(singleCellData1);
        list.add(singleCellData2);
        return list;
    }
}
