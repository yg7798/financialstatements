package com.tekion.accounting.fs.service.excelGeneration;

import com.tekion.accounting.commons.dealer.DealerConfig;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.beans.mappings.OemFsMapping;
import com.tekion.accounting.fs.common.utils.UserContextUtils;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.repos.OemFSMappingRepo;
import com.tekion.accounting.fs.repos.OemFsCellGroupRepo;
import com.tekion.accounting.fs.service.accountingService.AccountingService;
import com.tekion.accounting.fs.service.common.cache.CustomFieldConfig;
import com.tekion.accounting.fs.service.common.excelGeneration.dto.ESReportCallbackDto;
import com.tekion.accounting.fs.service.common.excelGeneration.dto.financialStatement.OEMMappingRequestDto;
import com.tekion.accounting.fs.service.common.excelGeneration.enums.ExcelReportType;
import com.tekion.dealersettings.dealermaster.beans.DealerMaster;
import com.tekion.as.models.beans.GLAccount;
import com.tekion.as.models.dto.MonthInfo;
import com.tekion.core.es.common.impl.TekFilterRequest;
import com.tekion.core.es.request.ESResponse;
import com.tekion.core.excelGeneration.models.model.FetchNextBatchRequest;
import com.tekion.core.excelGeneration.models.model.v2.FetchNextBatchRequestV2;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@PrepareForTest(UserContextUtils.class)
@RunWith(PowerMockRunner.class)
public class OEMMappingExcelHelperServiceTest extends TestCase {

    @Mock
    OemFSMappingRepo oemFSMappingRepo;
    @Mock
    OemFsCellGroupRepo oemFsCellGroupRepo;
    @Mock
    CustomFieldConfig customFieldConfig;
    @Mock
    FSEntryRepo fsEntryRepo;
    @Mock
    DealerConfig dealerConfig;
    @Mock
    AccountingService accountingService;
    @InjectMocks
    OEMMappingExcelHelperService helperService;

    @Before
    public void setUp() {
        DealerMaster dealerMaster = new DealerMaster();
        dealerMaster.setDealerName("dealer1");
        UserContextProvider.setContext(new UserContext("id1", "tenant1", "dealer1"));
        Mockito.when(dealerConfig.getDealerTimeZoneName()).thenReturn("local");
        Mockito.when(dealerConfig.getDealerMaster()).thenReturn(dealerMaster);
        Mockito.when(dealerConfig.getDealerCountryCode()).thenReturn("US");
        PowerMockito.mockStatic(System.class);
        PowerMockito.when(System.getenv(Mockito.anyString())).thenReturn("local");
    }

    @Test
    public void testGetExportableReportRows() {
        MonthInfo monthInfo = new MonthInfo();
        monthInfo.setMonth(1);
        monthInfo.setYear(2021);

        ESResponse esResponseLocal = new ESResponse();
        esResponseLocal.setHits(getGlAccounts());

        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(Mockito.anyString(), Mockito.anyString()))
                        .thenReturn(getFsEntry());
        Mockito.when(accountingService.getActiveMonthInfo())
                        .thenReturn(monthInfo);
        Mockito.when(accountingService.defaultSearch(Mockito.any()))
                        .thenReturn(esResponseLocal);
        Mockito.when(oemFSMappingRepo.findMappingsByFsId(Mockito.any(), Mockito.any()))
                        .thenReturn(getOEMMappingReportRow());
        helperService.getExportableReportRows(ExcelReportType.OEM_MAPPING, getOEMMappingExcelReportContext());
    }

    private OEMMappingExcelReportContext getOEMMappingExcelReportContext() {

        OEMMappingExcelReportContext context =
        new OEMMappingExcelReportContext();
        context.setFsEntry(getFsEntry());
        context.setFetchNextBatchRequest(getFetchNextBatchRequest());
        context.setGlAccountList(getGlAccounts());
        return context;
    }

    private List<GLAccount> getGlAccounts() {
        List<GLAccount> accountList = new ArrayList<>();
        GLAccount glAccount1 = new GLAccount();
        glAccount1.setAccountName("acc1");
        glAccount1.setAccountNumber("1234");
        glAccount1.setDealerId("4");
        glAccount1.setId("1234");
        glAccount1.setDeleted(false);
        accountList.add(glAccount1);
        return  accountList;
    }

    private FSEntry getFsEntry() {
        FSEntry fsEntry = new FSEntry();
        fsEntry.setDealerId("5");
        fsEntry.setYear(2021);
        fsEntry.setVersion(1);
        fsEntry.setOemId("Acura");
        fsEntry.setId("6155a7d8b3cb1f0006868cd6");
        fsEntry.setSiteId("-1_5");
        fsEntry.setFsType("CONSOLIDATED");
        fsEntry.setDealerIds(Arrays.asList("4", "5"));
        return fsEntry;
    }

    private FetchNextBatchRequest getFetchNextBatchRequest() {
        FetchNextBatchRequest fetchNextBatchRequest = new FetchNextBatchRequestV2();
        fetchNextBatchRequest.setAwsRequestId("awsReqId");
        fetchNextBatchRequest.setReportType("OEM_MAPPING");
        fetchNextBatchRequest.setOriginalPayload(getPayload());
        return fetchNextBatchRequest;
    }

    private List<OemFsMapping> getOEMMappingReportRow() {
        List<OemFsMapping> rows = new ArrayList<>();
        OemFsMapping row1 = new OemFsMapping();
        row1.setFsId("23456");
        row1.setDealerId("4");
        row1.setOemId("GM");
        row1.setYear(2021);
        row1.setVersion(1);
        row1.setId("2345");
        row1.setGlAccountId("1234");
        rows.add(row1);

        OemFsMapping row2 = new OemFsMapping();
        row2.setFsId("98765");
        row2.setDealerId("4");
        row2.setOemId("GM");
        row2.setYear(2021);
        row2.setVersion(1);
        row2.setId("9876");
        row2.setGlAccountId("1234");
        rows.add(row2);
        return rows;
     }

    private ESReportCallbackDto getPayload() {
        TekFilterRequest tekFilterRequest = TekFilterRequest.builder()
                .field("franchise")
                .key("oemId")
                .values(Arrays.asList("4", "5"))
                .build();

        OEMMappingRequestDto extraInfo = new OEMMappingRequestDto();
        extraInfo.setFsId("123456");
        extraInfo.setFilters(Arrays.asList(tekFilterRequest));

        ESReportCallbackDto esReportCallbackDto = new ESReportCallbackDto();
        esReportCallbackDto.setExtraInfoForCallback(extraInfo);
        esReportCallbackDto.setTargetDealerId("4");
        esReportCallbackDto.setTargetDealerId("5");
        return esReportCallbackDto;
    }
}
