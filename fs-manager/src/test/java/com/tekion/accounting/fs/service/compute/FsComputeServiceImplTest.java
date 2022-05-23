package com.tekion.accounting.fs.service.compute;

import com.tekion.accounting.commons.dealer.DealerConfig;
import com.tekion.accounting.fs.beans.common.AccountingOemFsCellCode;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.beans.common.OemConfig;
import com.tekion.accounting.fs.beans.mappings.OemFsMapping;
import com.tekion.accounting.fs.beans.mappings.OemFsMappingSnapshot;
import com.google.common.collect.Maps;
import com.tekion.accounting.fs.beans.common.*;
import com.tekion.accounting.fs.beans.mappings.*;
import com.tekion.accounting.fs.beans.memo.*;
import com.tekion.accounting.fs.common.dpProvider.DpUtils;
import com.tekion.accounting.fs.common.utils.TimeUtils;
import com.tekion.accounting.fs.dto.cellcode.FsCellCodeDetailsResponseDto;
import com.tekion.accounting.fs.dto.cellGrouop.FSCellGroupCodeCreateDto;
import com.tekion.accounting.fs.dto.cellGrouop.FSCellGroupCodesCreateDto;
import com.tekion.accounting.fs.dto.cellGrouop.FsGroupCodeDetailsResponseDto;
import com.tekion.accounting.fs.dto.cellcode.*;
import com.tekion.accounting.fs.dto.mappings.*;
import com.tekion.accounting.fs.dto.oemConfig.OemConfigRequestDto;
import com.tekion.accounting.fs.dto.oemTemplate.OemTemplateReqDto;
import com.tekion.accounting.fs.dto.oemTemplate.TemplateDetail;
import com.tekion.accounting.fs.enums.AccountType;
import com.tekion.accounting.fs.enums.FSType;
import com.tekion.accounting.fs.enums.FsCellCodeSource;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.repos.*;
import com.tekion.accounting.fs.repos.worksheet.HCWorksheetRepo;
import com.tekion.accounting.fs.repos.worksheet.MemoWorksheetRepo;
import com.tekion.accounting.fs.service.accountingInfo.AccountingInfoService;
import com.tekion.accounting.fs.service.accountingService.AccountingService;
import com.tekion.accounting.fs.service.common.cache.CustomFieldConfig;
import com.tekion.accounting.fs.service.fsEntry.FsEntryService;
import com.tekion.as.models.beans.*;
import com.tekion.as.models.dto.MonthInfo;
import com.tekion.audit.client.manager.AuditEventManager;
import com.tekion.audit.client.manager.impl.AuditEventDTO;
import com.tekion.beans.DynamicProperty;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.utils.UserContext;
import com.tekion.core.utils.UserContextProvider;
import com.tekion.core.validation.TValidator;
import com.tekion.dto.ClientDynamicPropertyItem;
import com.tekion.propertyclient.DPClient;
import com.tekion.util.DPClientPropertyCache;
import junit.framework.TestCase;

import org.assertj.core.util.Sets;
import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.tekion.accounting.fs.common.TConstants.ACCOUNTING_MODULE;
import static org.mockito.ArgumentMatchers.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DpUtils.class)
public class FsComputeServiceImplTest extends TestCase {

    private static final String FS_ROUND_OFF_PROPERTY = "FS_USE_PRECISION";

    @InjectMocks
    FsComputeServiceImpl oemMappingService;
    @InjectMocks
    TimeUtils timeUtils;

    @Mock
    FsEntryService fsEntryService;

    @Mock
    DealerConfig dealerConfig;
    @Mock
    FSCellCodeRepo fsCellCodeRepo;
    @Mock
    OemFsMappingSnapshotRepo oemFsMappingSnapshotRepo;
    @Mock
    MemoWorksheetRepo memoWorksheetRepo;
    @Mock
    HCWorksheetRepo hcWorksheetRepo;
    @Mock
    DPClient dpClient;
    @Mock
    OemConfigRepo oemConfigRepo;
    @Mock
    FSEntryRepo fsEntryRepo;
    @Mock
    AccountingInfoService aiService;
    @Mock
    OemFSMappingRepo oemFSMappingRepo;
    @Mock
    AccountingService accountingService;
    @Mock
    OEMFinancialMappingMediaRepository oemFSMediaRepo;
    @Mock
    OEMFinancialMappingRepository oemFinancialMappingRepo;
    @Mock
    TValidator validator;
    @Mock
    OemTemplateRepo oemTemplateRepo;
    @Mock
    OemFsCellGroupRepo oemFsCellGroupRepo;
    @Mock
    OEMFsCellCodeSnapshotRepo oemFsCellCodeSnapshotRepo;
    @Mock
    CustomFieldConfig customFieldConfig;
    @Mock
    AuditEventManager auditEventManager;

    private final List<String> EMPTY_LIST = new ArrayList<>();

    @Before
    public void setUp() throws ExecutionException {
        UserContextProvider.setContext(new UserContext("-1", "ca", "4"));
        Mockito.when(dealerConfig.getDealerTimeZone()).thenReturn(TimeZone.getTimeZone("America/Los_Angeles"));
        Mockito.when(dealerConfig.getDealerCountryCode()).thenReturn("US");
        Mockito.when(fsCellCodeRepo.getFsCellCodesForOemYearAndCountry(anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString())).
                thenReturn(getAccountingOemFsCellCodes());
        Mockito.when(dpClient.getStringProperty(ACCOUNTING_MODULE, FS_ROUND_OFF_PROPERTY)).
                thenReturn(new DynamicProperty<String>("",new DPClientPropertyCache()) {
                    @Override
                    protected String getTypedPropertyValue(ClientDynamicPropertyItem property, String defaultInErrorOrAbsent) {
                        return "";
                    }

                    @Override
                    protected String getTypedSafePropertyValue(ClientDynamicPropertyItem property, String defaultInErrorOrAbsent) {
                        return "";
                    }
                });
        Mockito.when(oemConfigRepo.findByOemId(anyString(), anyString())).thenReturn(getOemConfig());
        oemMappingService.postConstruct();
        Mockito.when(accountingService.getActiveMonthInfo()).
                thenReturn(getMonthInfo());
        Mockito.when(fsEntryRepo.findDefaultType(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(getFsEntry());
        Mockito.when(accountingService.getTrialBalanceReportForMonthV2(
                Mockito.anyInt(), Mockito.anyInt(), Mockito.any(), Mockito.anyBoolean(),
                Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(getTrialBalance());
        Mockito.when(oemFsCellCodeSnapshotRepo.findAllSnapshotByFsIdAndMonth(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(oemFsCellCodeSnapshotList());
        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(anyString(), anyString())).
                thenReturn(getFsEntry());
        Mockito.when(oemFSMappingRepo.findMappingsByFsId(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(oemFsMappingList());
        Mockito.when(oemFsCellGroupRepo.findNonDeletedByOemIdYearVersionAndCountry(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(getAccountingOemFsGroupCode());
        Mockito.when(dealerConfig.getDealerTimeZoneName()).thenReturn("UTC");
        Mockito.when(fsCellCodeRepo.findByCodesAndDealerIdAndOemIdNonDeleted(Mockito.anyList(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(getAccountingOemFsCellCodes());
        Mockito.when(accountingService.getCYTrialBalanceTillDayOfMonth(Mockito.anyLong(), Mockito.anySet(), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyBoolean())).
                thenReturn(getTrialBalance());
        Mockito.when(fsEntryRepo.findFsEntriesByYearRange(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                        .thenReturn(Arrays.asList(getFsEntry()));
        Mockito.when(memoWorksheetRepo.findForOemByYearOptimized(anyString(), anyString())).
                thenReturn(getMemoWorksheetList());
        mockStatic(DpUtils.class);
        PowerMockito.when(DpUtils.doUseTbGeneratorV2VersionForFsInOem()).thenReturn(false);
    }


    @Test
    public void computeFsCellCodeDetails() {
        Mockito.when(oemMappingService.getOemConfig("Acura")).thenReturn(getOemConfig());
        Mockito.when(accountingService.getActiveMonthInfo()).
                thenReturn(getMonthInfo());
        Mockito.when(oemFsMappingSnapshotRepo.findAllSnapshotByYearAndMonth(anyString(), Mockito.anyInt(), anyString())).
                thenReturn(getOemFsMappingSnapshotList());
        Mockito.when(memoWorksheetRepo.findForOemByYearOptimized(anyString(), anyString())).
                thenReturn(getMemoWorksheetList());
        Mockito.when(hcWorksheetRepo.findByFsId(anyString()))
                .thenReturn(getHCWorksheetList());
        Mockito.when(accountingService.getGlBalCntInfoForFS(Mockito.any()))
                .thenReturn(new HashMap<Integer, Map<String, Map<String, BigDecimal>>>() {
                });
        FsCellCodeDetailsResponseDto fsCellCodeDetailsResponseDto = oemMappingService.computeFsCellCodeDetailsByFsId("6155a7d8b3cb1f0006868cd6", 12345, false, false);
        assertNotNull(fsCellCodeDetailsResponseDto);
    }

    @Test
    public void computeFsCellCodeDetailsWithEpoch() {
        Mockito.when(oemMappingService.getOemConfig("Acura")).thenReturn(getOemConfig());
        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(anyString(), Mockito.anyString())).thenReturn(getFsEntry());
        Mockito.when(accountingService.getActiveMonthInfo()).
                thenReturn(getMonthInfo());
        Mockito.when(oemFsMappingSnapshotRepo.findAllSnapshotByYearAndMonth(anyString(), Mockito.anyInt(), anyString())).
                thenReturn(getOemFsMappingSnapshotList());
        Mockito.when(hcWorksheetRepo.findByFsId(anyString()))
                .thenReturn(getHCWorksheetList());
        Mockito.when(accountingService.getGlBalCntInfoForFS(Mockito.any()))
                .thenReturn(new HashMap<Integer, Map<String, Map<String, BigDecimal>>>() {
                });
        FsCellCodeDetailsResponseDto fsCellCodeDetailsResponseDto = oemMappingService.computeFsCellCodeDetailsByFsId("6155a7d8b3cb1f0006868cd6", 1599202256l, false, false);
        assertNotNull(fsCellCodeDetailsResponseDto);
    }

    @Test
    public void testGetFsCellCodeDetails() {
        Set<String> codes = Sets.newHashSet();
        codes.add("_204");
        Mockito.when(accountingService.getActiveMonthInfo()).
                thenReturn(getMonthInfo());
        oemMappingService.getFsCellCodeDetails("GM", 2021, 1, 3874638648L, codes, true, true );
    }

    @Test
    public void computeFsCellCodeDetailsForFS() {
        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(anyString(), Mockito.anyString())).thenReturn(getFsEntry());
        Mockito.when(accountingService.getActiveMonthInfo()).
                thenReturn(getMonthInfo());
        Mockito.when(accountingService.getAccountingSettings()).
                thenReturn(getAccountingSettings());
        Mockito.when(oemMappingService.getOemConfig("Acura")).thenReturn(getOemConfig());
        Mockito.when(oemFsMappingSnapshotRepo.findAllSnapshotByYearAndMonth(anyString(), Mockito.anyInt(), anyString())).
                thenReturn(getOemFsMappingSnapshotList());
        Mockito.when(accountingService.getFSTrialBalanceTillDayOfMonth(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyLong())).
                thenReturn(getTrialBalance());
        FsCellCodeDetailsResponseDto fsCellCodeDetailsResponseDto = oemMappingService.computeFsCellCodeDetailsForFS("6155a7d8b3cb1f0006868cd6", 1599202256l, false);
        assertNotNull(fsCellCodeDetailsResponseDto);
    }

    @Test
    public void testSaveMapping() {
        Mockito.doNothing().when(validator).validate(Mockito.any());
        Mockito.when(oemFinancialMappingRepo.deleteMappings(Mockito.anyList()))
                .thenReturn(null);
        Mockito.when(oemFinancialMappingRepo.upsertMappings(Mockito.anyList()))
                .thenReturn(null);
        Mockito.when(oemFSMediaRepo.saveMedia(Mockito.any())).thenReturn(null);
        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(getFsEntry());
        Mockito.when(oemFSMediaRepo.findSavedMediaByDealerIdNonDeleted(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(getFSMappingMedia());
        oemMappingService.saveMapping(getOemMappingRequestDto());
        Mockito.verify(oemFinancialMappingRepo).deleteMappings(Mockito.anyList());
        Mockito.verify(oemFinancialMappingRepo).upsertMappings(Mockito.anyList());
    }

    @Test
    public void testGetOEMMappingByDealerId() {
        Mockito.when(oemFSMediaRepo.findSavedMediaByDealerIdNonDeleted(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(getFSMappingMedia());
        Mockito.when(oemFinancialMappingRepo.findMappingsByFsIdAndDealerId(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(getOEMFinancialMappings());
        oemMappingService.getOEMMappingByDealerId(OEM.GM, "4", "2021");
        Mockito.verify(oemFSMediaRepo).findSavedMediaByDealerIdNonDeleted(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        Mockito.verify(oemFinancialMappingRepo).findMappingsByFsIdAndDealerId(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testGetOemTMappingList() {
        Mockito.when(dealerConfig.getDealerCountryCode())
                .thenReturn("US");
        oemMappingService.getOemTMappingList("GM", 2021, 1, "", true);
    }

    @Test
    public void testComputeFsCellCodeDetails() {
        MonthInfo monthInfo = getMonthInfo();
        monthInfo.setYear(2022);
        Mockito.when(fsEntryRepo.findDefaultType(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(getFsEntry());
        Mockito.when(accountingService.getActiveMonthInfo())
                .thenReturn(getMonthInfo(), monthInfo);
        Mockito.when(dealerConfig.getDealerCountryCode()).thenReturn("US");
        Assert.assertNotNull(oemMappingService.computeFsCellCodeDetails("GM", 2021, 1, 2021, 3, true, "_1-4", true));
    }

    @Test
    public void testComputeFsCellCodeDetails_memoWorksheet() {
        MonthInfo monthInfo = getMonthInfo();
        monthInfo.setYear(2022);
        Mockito.when(fsEntryRepo.findDefaultType(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(getFsEntry());
        Mockito.when(accountingService.getActiveMonthInfo())
                .thenReturn(getMonthInfo(), monthInfo);
        Mockito.when(dealerConfig.getDealerCountryCode()).thenReturn("US");
        Mockito.when(fsCellCodeRepo.getFsCellCodesForOemYearAndCountry(anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString())).
                thenReturn(getAccountingOemFsCellCodes_memoWorksheet());
        Assert.assertNotNull(oemMappingService.computeFsCellCodeDetails("GM", 2019, 1, 2019, 1, true, "_1-4", true));
    }

    @Test
    public void testComputeFsCellCodeDetails_dealerConfig() {
        MonthInfo monthInfo = getMonthInfo();
        monthInfo.setYear(2022);
        Mockito.when(fsEntryRepo.findDefaultType(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(getFsEntry());
        Mockito.when(accountingService.getActiveMonthInfo())
                .thenReturn(getMonthInfo(), monthInfo);
        Mockito.when(dealerConfig.getDealerCountryCode()).thenReturn("US");
        List<AccountingOemFsCellCode> accountingOemFsCellCodeList = getAccountingOemFsCellCodes_memoWorksheet();
        accountingOemFsCellCodeList.get(0).setSource(FsCellCodeSource.DEALER_CONFIG);
        Mockito.when(fsCellCodeRepo.getFsCellCodesForOemYearAndCountry(anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString())).
                thenReturn(accountingOemFsCellCodeList);
        Assert.assertNotNull(oemMappingService.computeFsCellCodeDetails("GM", 2019, 1, 2019, 1, true, "_1-4", true));
    }

    @Test
    public void testComputeFsCellCodeDetails_dealerConfigSource() {
        MonthInfo monthInfo = getMonthInfo();
        monthInfo.setYear(2022);
        Mockito.when(fsEntryRepo.findDefaultType(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(getFsEntry());
        Mockito.when(accountingService.getActiveMonthInfo())
                .thenReturn(getMonthInfo(), monthInfo);
        Mockito.when(dealerConfig.getDealerCountryCode()).thenReturn("US");
        List<AccountingOemFsCellCode> accountingOemFsCellCodeList = getAccountingOemFsCellCodes_memoWorksheet();
        accountingOemFsCellCodeList.get(0).setSource(FsCellCodeSource.CUSTOM_SOURCE);
        accountingOemFsCellCodeList.get(0).getAdditionalInfo().put("sourceType", "CITY_STATE");
        Mockito.when(fsCellCodeRepo.getFsCellCodesForOemYearAndCountry(anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString())).
                thenReturn(accountingOemFsCellCodeList);
        Assert.assertNotNull(oemMappingService.computeFsCellCodeDetails("GM", 2019, 1, 2019, 1, true, "_1-4", true));
    }

    @Test
    public void testComputeFsCellCodeDetails_dealerConfigAddress() {
        MonthInfo monthInfo = getMonthInfo();
        monthInfo.setYear(2022);
        Mockito.when(fsEntryRepo.findDefaultType(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(getFsEntry());
        Mockito.when(accountingService.getActiveMonthInfo())
                .thenReturn(getMonthInfo(), monthInfo);
        Mockito.when(dealerConfig.getDealerCountryCode()).thenReturn("US");
        List<AccountingOemFsCellCode> accountingOemFsCellCodeList = getAccountingOemFsCellCodes_memoWorksheet();
        accountingOemFsCellCodeList.get(0).setSource(FsCellCodeSource.CUSTOM_SOURCE);
        accountingOemFsCellCodeList.get(0).getAdditionalInfo().put("sourceType", "ADDRESS");
        Mockito.when(fsCellCodeRepo.getFsCellCodesForOemYearAndCountry(anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString())).
                thenReturn(accountingOemFsCellCodeList);
        Assert.assertNotNull(oemMappingService.computeFsCellCodeDetails("GM", 2019, 1, 2019, 1, true, "_1-4", true));
    }

    @Test
    public void testComputeFsCellCodeDetails_dealerConfigName() {
        MonthInfo monthInfo = getMonthInfo();
        monthInfo.setYear(2022);
        Mockito.when(fsEntryRepo.findDefaultType(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(getFsEntry());
        Mockito.when(accountingService.getActiveMonthInfo())
                .thenReturn(getMonthInfo(), monthInfo);
        Mockito.when(dealerConfig.getDealerCountryCode()).thenReturn("US");
        List<AccountingOemFsCellCode> accountingOemFsCellCodeList = getAccountingOemFsCellCodes_memoWorksheet();
        accountingOemFsCellCodeList.get(0).setSource(FsCellCodeSource.CUSTOM_SOURCE);
        accountingOemFsCellCodeList.get(0).getAdditionalInfo().put("sourceType", "DEALER_NAME");
        Mockito.when(fsCellCodeRepo.getFsCellCodesForOemYearAndCountry(anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString())).
                thenReturn(accountingOemFsCellCodeList);
        Assert.assertNotNull(oemMappingService.computeFsCellCodeDetails("GM", 2019, 1, 2019, 1, true, "_1-4", true));
    }

    @Test
    public void testComputeFsCellCodeDetails_dealerConfigFromDate() {
        MonthInfo monthInfo = getMonthInfo();
        monthInfo.setYear(2022);
        Mockito.when(fsEntryRepo.findDefaultType(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(getFsEntry());
        Mockito.when(accountingService.getActiveMonthInfo())
                .thenReturn(getMonthInfo(), monthInfo);
        Mockito.when(dealerConfig.getDealerCountryCode()).thenReturn("US");
        List<AccountingOemFsCellCode> accountingOemFsCellCodeList = getAccountingOemFsCellCodes_memoWorksheet();
        accountingOemFsCellCodeList.get(0).setSource(FsCellCodeSource.CUSTOM_SOURCE);
        accountingOemFsCellCodeList.get(0).getAdditionalInfo().put("sourceType", "FROM_DATE");
        Mockito.when(fsCellCodeRepo.getFsCellCodesForOemYearAndCountry(anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString())).
                thenReturn(accountingOemFsCellCodeList);
        Assert.assertNotNull(oemMappingService.computeFsCellCodeDetails("GM", 2019, 1, 2019, 1, true, "_1-4", true));
    }

    @Test
    public void testComputeFsCellCodeDetails_dealerConfigToDate() {
        MonthInfo monthInfo = getMonthInfo();
        monthInfo.setYear(2022);
        Mockito.when(fsEntryRepo.findDefaultType(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(getFsEntry());
        Mockito.when(accountingService.getActiveMonthInfo())
                .thenReturn(getMonthInfo(), monthInfo);
        Mockito.when(dealerConfig.getDealerCountryCode()).thenReturn("US");
        List<AccountingOemFsCellCode> accountingOemFsCellCodeList = getAccountingOemFsCellCodes_memoWorksheet();
        accountingOemFsCellCodeList.get(0).setSource(FsCellCodeSource.CUSTOM_SOURCE);
        accountingOemFsCellCodeList.get(0).getAdditionalInfo().put("sourceType", "TO_DATE");
        Mockito.when(fsCellCodeRepo.getFsCellCodesForOemYearAndCountry(anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString())).
                thenReturn(accountingOemFsCellCodeList);
        Assert.assertNotNull(oemMappingService.computeFsCellCodeDetails("GM", 2019, 1, 2019, 1, true, "_1-4", true));
    }

    @Test
    public void testComputeFsCellCodeDetails_dateMonth() {
        MonthInfo monthInfo = getMonthInfo();
        monthInfo.setYear(2022);
        Mockito.when(fsEntryRepo.findDefaultType(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(getFsEntry());
        Mockito.when(accountingService.getActiveMonthInfo())
                .thenReturn(getMonthInfo(), monthInfo);
        Mockito.when(dealerConfig.getDealerCountryCode()).thenReturn("US");
        List<AccountingOemFsCellCode> accountingOemFsCellCodeList = getAccountingOemFsCellCodes_memoWorksheet();
        accountingOemFsCellCodeList.get(0).setSource(FsCellCodeSource.DATE_MONTH);
        Mockito.when(fsCellCodeRepo.getFsCellCodesForOemYearAndCountry(anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString())).
                thenReturn(accountingOemFsCellCodeList);
        Assert.assertNotNull(oemMappingService.computeFsCellCodeDetails("GM", 2019, 1, 2019, 1, true, "_1-4", true));
    }

    @Test
    public void testComputeFsCellCodeDetails_customSource() {
        MonthInfo monthInfo = getMonthInfo();
        monthInfo.setYear(2022);
        Mockito.when(fsEntryRepo.findDefaultType(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(getFsEntry());
        Mockito.when(accountingService.getActiveMonthInfo())
                .thenReturn(getMonthInfo(), monthInfo);
        Mockito.when(dealerConfig.getDealerCountryCode()).thenReturn("US");
        List<AccountingOemFsCellCode> accountingOemFsCellCodeList = getAccountingOemFsCellCodes_memoWorksheet();
        accountingOemFsCellCodeList.get(0).setSource(FsCellCodeSource.CUSTOM_SOURCE);
        Mockito.when(fsCellCodeRepo.getFsCellCodesForOemYearAndCountry(anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString())).
                thenReturn(accountingOemFsCellCodeList);
        Assert.assertNotNull(oemMappingService.computeFsCellCodeDetails("GM", 2019, 1, 2019, 1, true, "_1-4", true));
    }

    @Test
    public void testComputeFsCellCodeDetails_hcWorksheet() {
        MonthInfo monthInfo = getMonthInfo();
        monthInfo.setYear(2022);
        Mockito.when(fsEntryRepo.findDefaultType(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(getFsEntry());
        Mockito.when(accountingService.getActiveMonthInfo())
                .thenReturn(getMonthInfo(), monthInfo);
        Mockito.when(dealerConfig.getDealerCountryCode()).thenReturn("US");
        Mockito.when(fsCellCodeRepo.getFsCellCodesForOemYearAndCountry(anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString())).
                thenReturn(getAccountingOemFsCellCodes_hcWorksheet());
        Assert.assertNotNull(oemMappingService.computeFsCellCodeDetails("GM", 2019, 1, 2019, 1, true, "_1-4", true));
    }

    @Test
    public void testComputeFsGroupCodeDetails_ifNotFutureMonth() {
        Mockito.when(fsEntryRepo.findByOemYearVersionAndSite(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyString(), Mockito.anyString())).thenReturn(getFsEntries());
        Mockito.when(fsEntryRepo.findDefaultType(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(getFsEntry());
        Mockito.when(accountingService.getActiveMonthInfo()).thenReturn(getMonthInfo());
        Mockito.when(oemFsMappingSnapshotRepo.findAllSnapshotByYearAndMonth(anyString(), anyInt(), anyString())).thenReturn(getOemFsMappingSnapshotList());
        FsGroupCodeDetailsResponseDto responseDto = oemMappingService.computeFsGroupCodeDetails("GM", 2021, 1, 474384L, true, true, "-1_4" );
        assertEquals(1, responseDto.getDetails().size());
    }

    @Test
    public void testComputeFsGroupCodeDetails_ifFutureMonth() {
        Mockito.when(fsEntryRepo.findByOemYearVersionAndSite(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyString(), Mockito.anyString())).thenReturn(getFsEntries());
        Mockito.when(fsEntryRepo.findDefaultType(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(getFsEntry());
        Mockito.when(accountingService.getActiveMonthInfo()).thenReturn(getMonthInfo());
        FsGroupCodeDetailsResponseDto responseDto = oemMappingService.computeFsGroupCodeDetails("GM", 2000, 1, 4743844366846L, true, true, "-1_4" );
        assertNull(responseDto.getDetails());
        assertNull(responseDto.getDate());
    }

    @Test
    public void testCreateFsmInfoIfPresent_ifFSEntryPresent() {
        Mockito.when(fsEntryRepo.findByOem(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(getFsEntry());
        Mockito.when(fsEntryService.createFSEntry(Mockito.any())).thenReturn(getFsEntry());
        oemMappingService.createFsmInfoIfPresent(OEM.GM, 2021, 1);
        Mockito.verify(fsEntryRepo, Mockito.times(1)).findByOem(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString());
        Mockito.verify(fsEntryService, Mockito.times(1)).createFSEntry(Mockito.any());
    }

    @Test
    public void testCreateFsmInfoIfPresent_ifFSEntryNotPresent() {
        Mockito.when(fsEntryRepo.findByOem(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(null);
        oemMappingService.createFsmInfoIfPresent(OEM.GM, 2021, 1);
        Mockito.verify(fsEntryRepo, Mockito.times(1)).findByOem(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString());
        Mockito.verify(fsEntryService, Mockito.times(0)).createFSEntry(Mockito.any());
    }

    @Test
    public void isFutureMonth_true(){
        assertTrue(oemMappingService.isFutureMonth(getMonthInfo(), 2999, 10));
    }

    @Test
    public void isFutureMonth_false(){
        assertFalse(oemMappingService.isFutureMonth(getMonthInfo(), 2019, 10));
    }


    @Test
    public void testComputeFsDetails() {
        Mockito.when(fsEntryRepo.findDefaultType(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(getFsEntry());
        Mockito.when(memoWorksheetRepo.findForOemByYearOptimized(Mockito.anyString(), Mockito.anyString()))
                        .thenReturn(getMemoWorksheetList());
        assertNotNull(oemMappingService.computeFsDetails("GM", 2000, 1, 453455L, true, true));
        Mockito.verify(fsEntryRepo, Mockito.times(1)).findDefaultType(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString());
        Mockito.verify(accountingService, Mockito.times(1)).getActiveMonthInfo();
    }

    @Test
    public void testAddFsTypeInOemFsCellCodeSnapshots() {
        Mockito.when(fsEntryRepo.getFSEntries(Mockito.anyString()))
                .thenReturn(Arrays.asList(getFsEntry()));
        Mockito.doNothing().when(oemFsCellCodeSnapshotRepo)
                .updateFsTypeInFsCellCodeSnapshots(Mockito.any());
        oemMappingService.addFsTypeInOemFsCellCodeSnapshots("4");
        Mockito.verify(fsEntryRepo, Mockito.times(1))
                .getFSEntries(Mockito.anyString());
        Mockito.verify(oemFsCellCodeSnapshotRepo, Mockito.times(1))
                .updateFsTypeInFsCellCodeSnapshots(Mockito.any());
    }

    @Test
    public void testMigrateOemFsMappingSnapshotsFromOemToFSLevel() {
        Mockito.when(fsEntryRepo.getFSEntries(Mockito.anyString()))
                .thenReturn(Arrays.asList(getFsEntry()));
        Mockito.doNothing().when(oemFsMappingSnapshotRepo)
                .updateFsIdInOemFsMappingSnapshots(Mockito.any());
        oemMappingService.migrateOemFsMappingSnapshotsFromOemToFSLevel("4");
        Mockito.verify(fsEntryRepo, Mockito.times(1))
                .getFSEntries(Mockito.anyString());
        Mockito.verify(oemFsMappingSnapshotRepo, Mockito.times(1))
                .updateFsIdInOemFsMappingSnapshots(Mockito.any());
    }

    @Test
    public void testMigrateOemFsMappingFromOemToFSLevel() {
        Mockito.when(fsEntryRepo.getFSEntries(Mockito.anyString()))
                .thenReturn(Arrays.asList(getFsEntry()));
        Mockito.doNothing().when(oemFSMappingRepo)
                .updateFsIdInOemFsMapping(Mockito.any());
        oemMappingService.migrateOemFsMappingFromOemToFSLevel("4");
        Mockito.verify(fsEntryRepo, Mockito.times(1))
                .getFSEntries(Mockito.anyString());
        Mockito.verify(oemFSMappingRepo, Mockito.times(1))
                .updateFsIdInOemFsMapping(Mockito.any());
    }

    @Test
    public void testMigrateOemFsCellCodeSnapshotsFromOemToFSLevel() {
        Mockito.when(fsEntryRepo.getFSEntries(Mockito.anyString()))
                .thenReturn(Arrays.asList(getFsEntry()));
        Mockito.doNothing().when(oemFsCellCodeSnapshotRepo)
                .updateFsIdInOemFsCellCodeSnapshots(Mockito.any());
        oemMappingService.migrateOemFsCellCodeSnapshotsFromOemToFSLevel("4");
        Mockito.verify(fsEntryRepo, Mockito.times(1))
                .getFSEntries(Mockito.anyString());
        Mockito.verify(oemFsCellCodeSnapshotRepo, Mockito.times(1))
                .updateFsIdInOemFsCellCodeSnapshots(Mockito.any());
    }


    @Test
    public void testDeleteGroupCodes() {
        Mockito.when(oemFsCellGroupRepo.delete(Mockito.any()))
                .thenReturn(new ArrayList<>());
        oemMappingService.deleteGroupCodes("GM", 2021, EMPTY_LIST, 1,"US");
        Mockito.verify(oemFsCellGroupRepo, Mockito.times(1)).findNonDeletedByOemIdYearVersionAndCountry(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString());
        Mockito.verify(oemFsCellGroupRepo, Mockito.times(1)).delete(Mockito.any());
    }

    @Test
    public void testDeleteMappingSnapshots() {
        Mockito.doNothing().when(oemFsMappingSnapshotRepo).deleteSnapshots(Mockito.anyString(), Mockito.any(), Mockito.anyString());
        oemMappingService.deleteMappingSnapshots(getMappingSnapshotDto());
        Mockito.verify(oemFsMappingSnapshotRepo, Mockito.times(1)).deleteSnapshots(Mockito.anyString(), Mockito.any(), Mockito.anyString());
    }

    @Test
    public void testCreateSnapshotsForMapping_Success() {
        Mockito.when(oemFsMappingSnapshotRepo.findOneSnapshot(Mockito.anyString(), Mockito.anyList(), Mockito.anyString()))
                .thenReturn(null);
        Mockito.doNothing().when(oemFsMappingSnapshotRepo).saveBulkSnapshot(Mockito.anyList());
        oemMappingService.createSnapshotsForMapping(getMappingSnapshotDto());
        Mockito.verify(oemFsMappingSnapshotRepo, Mockito.times(1)).findOneSnapshot(Mockito.anyString(), Mockito.anyList(), Mockito.anyString());
        Mockito.verify(oemFSMappingRepo, Mockito.times(1)).findMappingsByFsId(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(oemFsMappingSnapshotRepo, Mockito.times(1)).saveBulkSnapshot(Mockito.anyList());
    }

    @Test( expected = TBaseRuntimeException.class)
    public void testCreateSnapshotsForMapping_Exception() {
        Mockito.when(oemFsMappingSnapshotRepo.findOneSnapshot(Mockito.anyString(), Mockito.anyList(), Mockito.anyString()))
                .thenReturn(getOemFsMappingSnapshotList().get(0));
        oemMappingService.createSnapshotsForMapping(getMappingSnapshotDto());
        Mockito.verify(oemFsMappingSnapshotRepo, Mockito.times(1)).findOneSnapshot(Mockito.anyString(), Mockito.anyList(), Mockito.anyString());
        Mockito.verify(oemFSMappingRepo, Mockito.times(0)).findMappingsByFsId(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(oemFsMappingSnapshotRepo, Mockito.times(0)).saveBulkSnapshot(Mockito.anyList());
    }

    @Test
    public void testSaveFsCellCodes() {
        FSCellCodeInfoRequest request = new FSCellCodeInfoRequest();
        request.setCode("204");
        request.setDerived(false);
        request.setGroupCode("_204");
        request.setDisplayName("204");
        request.setOemCode("GM");

        FSCellCodeInfoRequest request2 = new FSCellCodeInfoRequest();
        request2.setCode("309");
        request2.setDerived(false);
        request2.setGroupCode("_309");
        request2.setDisplayName("309");
        request2.setOemCode("GM");

        FSCellCodeListCreateDto dto = new FSCellCodeListCreateDto();
        dto.setYear(2021);
        dto.setVersion(1);
        dto.setCountry("US");
        dto.setOemId(OEM.GM);
        dto.setFsCellCodeDetails(Arrays.asList(request, request2));


        Mockito.when(fsCellCodeRepo.getFsCellCodesForOemYearAndCountry(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString()))
                        .thenReturn(getAccountingOemFsCellCodes());
        Mockito.when(fsCellCodeRepo.updateBulk(Mockito.anyList()))
                        .thenReturn(null);
        Mockito.when(fsCellCodeRepo.findByCodesAndDealerIdAndOemIdNonDeleted(Mockito.anyList(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                        .thenReturn(null);
        oemMappingService.saveFsCellCodes(dto);
        Mockito.verify(fsCellCodeRepo, Mockito.times(1)).getFsCellCodesForOemYearAndCountry(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString());
        Mockito.verify(fsCellCodeRepo, Mockito.times(1)).updateBulk(Mockito.anyList());
        Mockito.verify(fsCellCodeRepo, Mockito.times(1)).findByCodesAndDealerIdAndOemIdNonDeleted(Mockito.anyList(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString());
    }


    @Test
    public void testSaveTemplate() {
        TemplateDetail detail = new TemplateDetail();
        detail.setActive(true);
        detail.setCountry("US");
        detail.setVersion(1);
        detail.setOemId(OEM.GM);
        detail.setYear(2020);
        OemTemplateReqDto dto = new OemTemplateReqDto();
        dto.setTemplateDetails(Arrays.asList(detail));
        Mockito.doNothing().when(oemTemplateRepo).updateTemplatesAsInactive(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString());
        Mockito.when(oemTemplateRepo.updateBulk(Mockito.anyList()))
                .thenReturn(null);
        oemMappingService.saveTemplate(dto);
    }


    @Test
    public void testGetOemTemplate() {
        Mockito.when(oemTemplateRepo.findActiveTemplateByOemYearAndCountry(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(null);
        oemMappingService.getOemTemplate("GM", 2021);
        Mockito.verify(oemTemplateRepo, Mockito.times(1))
                .findActiveTemplateByOemYearAndCountry(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString());
    }

    @Test
    public void testSaveFsCellGroupCode() {
        FSCellGroupCodeCreateDto dto = getFsCellGroupCodeCreateDto();
        Mockito.when(oemFsCellGroupRepo.save(Mockito.any()))
                .thenReturn(null);
        oemMappingService.saveFsCellGroupCode(dto);
        Mockito.verify(oemFsCellGroupRepo, Mockito.times(1))
                .save(Mockito.any());
    }

    @Test
    public void testFetchFsCellGroupCodes() {
        Mockito.when(oemFsCellGroupRepo.findNonDeletedByOemIdYearVersionAndCountry(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(new ArrayList<>());
        oemMappingService.fetchFsCellGroupCodes("GM", 2021, 1);
        Mockito.verify(oemFsCellGroupRepo, Mockito.times(1))
                .findNonDeletedByOemIdYearVersionAndCountry(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString());
    }

    @Test
    public void testFetchFsCellGroupCodesInBulk() {
        Set<OEM> oems = new HashSet<>();
        oems.add(OEM.GM);
        oems.add(OEM.Acura);
        Mockito.when(oemFsCellGroupRepo.findByOemIdsAndYearNonDeleted(Mockito.anySet(), Mockito.anyInt(), Mockito.anyString()))
                        .thenReturn(null);
        oemMappingService.fetchFsCellGroupCodesInBulk(2021, oems);
        Mockito.verify(oemFsCellGroupRepo, Mockito.times(1))
                .findByOemIdsAndYearNonDeleted(Mockito.anySet(), Mockito.anyInt(), Mockito.anyString());

    }

    @Test
    public void testSaveFsCellGroupCodes() {
        Mockito.doNothing().when(oemFsCellGroupRepo).insertBulk(Mockito.anyList());
        oemMappingService.saveFsCellGroupCodes(getFSCellGroupCodesCreateDto());
        Mockito.verify(oemFsCellGroupRepo, Mockito.times(1))
                .insertBulk(Mockito.anyList());
    }

    @Test
    public void testUpsertFsCellGroupCodes() {
        Mockito.when(oemFsCellGroupRepo.upsertBulk(Mockito.anyList()))
                .thenReturn(new ArrayList<>());
        Mockito.when(oemFsCellGroupRepo.findByOemIdsAndGroupCodes(Mockito.anyList(), Mockito.anyList(), Mockito.anyList(), Mockito.anyString())).thenReturn(getAccountingOemFsGroupCode());
        oemMappingService.upsertFsCellGroupCodes(getFSCellGroupCodesCreateDto());
        Mockito.verify(oemFsCellGroupRepo, Mockito.times(1))
                .upsertBulk(Mockito.anyList());
    }

    @Test
    public void testAuditEventForSaveFsCellGroupCodes(){
        Mockito.doNothing().when(oemFsCellGroupRepo).insertBulk(Mockito.anyList());
        oemMappingService.saveFsCellGroupCodes(getFSCellGroupCodesCreateDto());
        Mockito.doNothing().when(auditEventManager).publishEvents(Mockito.any(AuditEventDTO.class));
        Mockito.verify(oemFsCellGroupRepo, Mockito.times(1))
                .insertBulk(Mockito.anyList());
    }

    @Test
    public void testAuditEventForUpsertFsCellGroupCodes(){
        Mockito.when(oemFsCellGroupRepo.upsertBulk(Mockito.anyList()))
                .thenReturn(new ArrayList<>());
        Mockito.when(oemFsCellGroupRepo.findByOemIdsAndGroupCodes(Mockito.anyList(), Mockito.anyList(), Mockito.anyList(), Mockito.anyString())).thenReturn(getAccountingOemFsGroupCode());
        oemMappingService.upsertFsCellGroupCodes(getFSCellGroupCodesCreateDto());
        Mockito.doNothing().when(auditEventManager).publishEvents(Mockito.any(AuditEventDTO.class));
        Mockito.verify(oemFsCellGroupRepo, Mockito.times(1))
                .upsertBulk(Mockito.anyList());
    }

    @Test
    public void testMigrateFsCellCodesFromGroup() {
        Mockito.when(oemFsCellGroupRepo.findByOemId(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(getAccountingOemFsGroupCode());
        Mockito.doNothing().when(fsCellCodeRepo).insertBulk(Mockito.anyList());
        oemMappingService.migrateFsCellCodesFromGroup(OEM.GM);
        Mockito.verify(fsCellCodeRepo, Mockito.times(1))
                .insertBulk(Mockito.anyList());
        Mockito.verify(oemFsCellGroupRepo, Mockito.times(1))
                .findByOemId(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testCreateFsCellCodeSnapshotForYearAndMonth_whenSnapshotNotExist() {
        CellCodeSnapshotCreateDto dto = new CellCodeSnapshotCreateDto();
        dto.setMonth(2);
        dto.setFsId("1244");
        dto.setAddM13BalInDecBalances(true);
        dto.setIncludeM13(true);
        Mockito.when(fsEntryRepo.findByIdAndDealerIdWithNullCheck(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(getFsEntry());
        Mockito.when(oemFsCellCodeSnapshotRepo.findOneSnapshotByFsIdAndMonth(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(null);
        Mockito.doNothing().when(oemFsCellCodeSnapshotRepo).saveBulkSnapshot(Mockito.anyList());
        oemMappingService.createFsCellCodeSnapshotForYearAndMonth(dto);
        Mockito.verify(fsEntryRepo, Mockito.times(1))
                .findByIdAndDealerIdWithNullCheck(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(oemFsCellCodeSnapshotRepo, Mockito.times(1))
                .findOneSnapshotByFsIdAndMonth(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString());
    }


    @Test(
            expected = TBaseRuntimeException.class
    )
    public void testMigrateGroupsCodesToYear_whenExists() {
        Mockito.when(oemFsCellGroupRepo.findByOemId(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(getAccountingOemFsGroupCode());
        oemMappingService.migrateGroupsCodesToYear("GM", 2019, 2021, "US");
    }

    @Test
    public void testMigrateGroupsCodesToYear_whenFromYearDoesNotExists() {
        Mockito.when(oemFsCellGroupRepo.findByOemId(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(null);
       Assert.assertEquals(0, oemMappingService.migrateGroupsCodesToYear("GM", 2019, 2021, "US").size());
    }

    @Test
    public void testMigrateGroupsCodesToYear_success() {
        Mockito.when(oemFsCellGroupRepo.findByOemId(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(null,getAccountingOemFsGroupCode());
        Assert.assertEquals(2, oemMappingService.migrateGroupsCodesToYear("GM", 2019, 2021, "US").size());
    }

    @Test(
            expected = TBaseRuntimeException.class
    )
    public void testMigrateCellCodesToYear_toYearExists() {
        Mockito.when(fsCellCodeRepo.getFsCellCodesForOemYearAndCountry(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(getAccountingOemFsCellCodes());
        oemMappingService.migrateCellCodesToYear("GM", 2019, 2021, "US").size();
    }

    @Test
    public void testMigrateCellCodesToYear_fromYearNotExists() {
        Mockito.when(fsCellCodeRepo.getFsCellCodesForOemYearAndCountry(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(null);
        Assert.assertEquals(0, oemMappingService.migrateCellCodesToYear("GM", 2019, 2021, "US").size());
    }

    @Test
    public void testMigrateCellCodesToYear_success() {
        Mockito.when(fsCellCodeRepo.getFsCellCodesForOemYearAndCountry(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(null, getAccountingOemFsCellCodes());
        Assert.assertEquals(3, oemMappingService.migrateCellCodesToYear("GM", 2019, 2021, "US").size());
    }


    @Test
    public void testDeleteCellCodes() {
        Mockito.when(oemFsCellGroupRepo.findByOemId(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(null);
        FsCellCodeDeleteDto dto = new FsCellCodeDeleteDto();
        dto.setCountry("US");
        dto.setYear(2021);
        dto.setOemId(OEM.GM);
        dto.setCellCodes(Arrays.asList("1","2"));
        Assert.assertEquals(3,oemMappingService.deleteCellCodes(dto).size());
    }

    @Test
    public void testPopulateGroupCodesInFsCell(){
        Mockito.when(fsCellCodeRepo.updateBulk(Mockito.anyList()))
                .thenReturn(null);
        oemMappingService.populateGroupCodesInFsCell(OEM.GM, 2021, 1);
        Mockito.verify(fsCellCodeRepo, Mockito.times(1)).updateBulk(Mockito.anyList());
    }

    @Test
    public void testGetFsCellCodeSnapshots() {
        oemMappingService.getFsCellCodeSnapshots("1234", 1);
        Mockito.verify(oemFsCellCodeSnapshotRepo, Mockito.times(1))
                .findAllSnapshotByFsIdAndMonth(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString());
    }

    @Test(expected = TBaseRuntimeException.class)
    public void testCreateFsCellCodeSnapshot_exception() {
        Mockito.when(oemFsCellCodeSnapshotRepo.findOneSnapshotByFsIdAndMonth(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(oemFsCellCodeSnapshotList().get(0));
        oemMappingService.createFsCellCodeSnapshot(getFsEntry(), 2021, 1, true, true);
    }

    @Test
    public void testCreateFsCellCodeSnapshot_success() {
        Mockito.when(oemFsCellCodeSnapshotRepo.findOneSnapshotByFsIdAndMonth(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(null);
        Mockito.doNothing().when(oemFsCellCodeSnapshotRepo).saveBulkSnapshot(Mockito.anyList());
        FSEntry fsEntry = getFsEntry();
        fsEntry.setYear(2000);
        oemMappingService.createFsCellCodeSnapshot(fsEntry, 2010, 1, true, true);
    }

    @Test
    public void testCreateBulkFsCellCodeSnapshot_success() {
        try {
            oemMappingService.createBulkFsCellCodeSnapshot("_1-4", "GM", 2019, 1, 2019, 3, 10, true, true);
        }catch (TBaseRuntimeException e){
            fail();
        }
    }

    @Test(expected = TBaseRuntimeException.class)
    public void testCreateBulkFsCellCodeSnapshot_exception() {
            oemMappingService.createBulkFsCellCodeSnapshot("_1-4", "GM", 2020, 1, 2020, 3, 10, true, true);
    }

    @Test
    public void testDeleteSnapshotByYearAndMonth() {
        Mockito.doNothing().when(oemFsCellCodeSnapshotRepo).deleteSnapshotByFsIdAndMonth(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString());
        assertTrue(oemMappingService.deleteSnapshotByYearAndMonth("-1-4", "GM", 2021, 1 , 2));
    }

    @Test
    public void testDeleteSnapshotsInBulk(){
        Mockito.when(fsEntryRepo.findByOemYearVersionAndSite(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString())).thenReturn(getFsEntries());
        Mockito.doNothing().when(oemFsCellCodeSnapshotRepo).hardDeleteSnapshotsInBulk(Mockito.anyList(), Mockito.anyList(), Mockito.anyString());
        assertTrue(oemMappingService.deleteSnapshotsInBulk(getFsCellCodeSnapshotDto()));
    }

    @Test
    public void testDeleteBulkSnapshotByYearAndMonth() {
        Mockito.doNothing().when(oemFsCellCodeSnapshotRepo).deleteBulkSnapshotByYearAndMonth(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString());
        assertTrue(oemMappingService.deleteBulkSnapshotByYearAndMonth("-1-4", "GM", 2021, 1 , 2));
    }

    @Test
    public void testSaveOemConfig_oemConfigExists() {
        OemConfigRequestDto dto = new OemConfigRequestDto();
        dto.setOemId(OEM.GM);
        dto.setCountry("US");
        oemMappingService.saveOemConfig(dto);
        Mockito.verify(oemConfigRepo, Mockito.timeout(1))
                .findByOemId(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testSaveOemConfig_oemConfigDoesNotExists() {
        OemConfigRequestDto dto = new OemConfigRequestDto();
        dto.setOemId(OEM.GM);
        dto.setCountry("US");
        Mockito.when(oemConfigRepo.findByOemId(anyString(), anyString())).thenReturn(null);
        oemMappingService.saveOemConfig(dto);
        Mockito.verify(oemConfigRepo, Mockito.timeout(1))
                .findByOemId(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(oemConfigRepo, Mockito.times(1))
                .save(Mockito.any());
    }

    @Test
    public void testGetAllOEMFsCellCodeSnapshotSummary_whenSnapshotAvailable() {
        oemMappingService.getAllOEMFsCellCodeSnapshotSummary("-1_4", "GM", 1,2019, 2, 2019, true, true);
        Mockito.verify(fsEntryRepo, Mockito.times(1))
                .findDefaultType(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testGetAllOEMFsCellCodeSnapshotSummary_whenSnapsAvailable() {
        oemMappingService.getAllOEMFsCellCodeSnapshotSummary("-1_4", "GM", 1,2022, 12, 2019, true, true);
        Mockito.verify(fsEntryRepo, Mockito.times(2))
                .findDefaultType(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString());
    }

    @Test(
            expected = TBaseRuntimeException.class
    )
    public void testCreateFsMappingSnapshot_whenSnapshotAvailable() {
        Mockito.when(oemFsMappingSnapshotRepo.findOneSnapshotByYearAndMonth(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(getOemFsMappingSnapshotList().get(0));
        oemMappingService.createFsMappingSnapshot("1234", 2);
    }

    @Test
    public void testCreateFsMappingSnapshot_whenSnapshotNotAvailable() {
        Mockito.when(oemFsMappingSnapshotRepo.findOneSnapshotByYearAndMonth(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(null);
        Mockito.doNothing().when(oemFsMappingSnapshotRepo).saveBulkSnapshot(Mockito.anyList());
        oemMappingService.createFsMappingSnapshot("1234", 2);
        Mockito.verify(oemFsMappingSnapshotRepo, Mockito.times(1))
                .findOneSnapshotByYearAndMonth(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString());
        Mockito.verify(oemFsMappingSnapshotRepo, Mockito.times(1))
                .saveBulkSnapshot(Mockito.anyList());
    }

    @Test( expected = TBaseRuntimeException.class )
    public void testCreateFsMappingSnapshotBulk_exception() {
            oemMappingService.createFsMappingSnapshotBulk("-1_4", "GM", 2020, 1, 2020, 1, 6);
    }

    @Test
    public void testCreateFsMappingSnapshotBulk_success() {
        try {
            oemMappingService.createFsMappingSnapshotBulk("-1_4", "GM", 2019, 1, 2019, 1, 6);
        } catch (TBaseRuntimeException e){
            fail();
        }
        Mockito.verify(fsEntryRepo, Mockito.times(1))
                .findDefaultType(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testCreateFsMappingAndCellCodeSnapshot_success() {
        Mockito.when(fsEntryRepo.findFsEntriesForYear(Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Arrays.asList(getFsEntry()));
        oemMappingService.createFsMappingAndCellCodeSnapshot(2021, 2, true, "-1_4", true);
        Mockito.verify(fsEntryRepo, Mockito.times(1))
                .findFsEntriesForYear(Mockito.anyInt(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testCreateFsMappingAndCellCodeSnapshot_exception() {
        Mockito.when(oemFsCellCodeSnapshotRepo.findOneSnapshotByFsIdAndMonth(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(oemFsCellCodeSnapshotList().get(0));
        Mockito.when(fsEntryRepo.findFsEntriesForYear(Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Arrays.asList(getFsEntry()));
        oemMappingService.createFsMappingAndCellCodeSnapshot(2021, 2, true, "-1_4", true);
        Mockito.verify(oemFsMappingSnapshotRepo, Mockito.times(0))
                .saveBulkSnapshot(Mockito.anyList());
    }

    @Test
    public void testSaveFsCellCode_whenNotNull() {
        FSCellCodeCreateDto dto = new FSCellCodeCreateDto();
        dto.setCode("A");
        dto.setCountry("US");
        dto.setYear(2021);
        dto.setOemId(OEM.GM);
        Mockito.when(fsCellCodeRepo.findByCodeOemIdYearAndCountry(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(getAccountingOemFsCellCodes().get(0));
        oemMappingService.saveFsCellCode(dto);
        Mockito.verify(fsCellCodeRepo, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    public void testSaveFsCellCode_whenNull() {
        FSCellCodeCreateDto dto = new FSCellCodeCreateDto();
        dto.setCode("A");
        dto.setCountry("US");
        dto.setYear(2021);
        dto.setOemId(OEM.GM);
        Mockito.when(fsCellCodeRepo.findByCodeOemIdYearAndCountry(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(null);
        oemMappingService.saveFsCellCode(dto);
        Mockito.verify(fsCellCodeRepo, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    public void testUpdateOemCode() {
        OemCodeUpdateDto dto = new OemCodeUpdateDto();
        dto.setOemId(OEM.GM);
        dto.setYear(2021);
        dto.setCountry("us");

        OemCodeUpdateDto.CodeUpdate update1 = new OemCodeUpdateDto.CodeUpdate();
        update1.setCode("203");
        update1.setOemCode("GM");
        OemCodeUpdateDto.CodeUpdate update2 = new OemCodeUpdateDto.CodeUpdate();
        update2.setCode("204");
        update2.setOemCode("GM");
        OemCodeUpdateDto.CodeUpdate update3 = new OemCodeUpdateDto.CodeUpdate();
        update3.setCode("205");
        update3.setOemCode("GM");

        Map<String, String> additionalInfo = new HashMap<>();
        additionalInfo.put("downloadExcel", "true");
        update1.setAdditionalInfo(additionalInfo);
        dto.setCodes(Arrays.asList(update1, update2, update3));

        OemCodeUpdateDto response = oemMappingService.updateOemCode(dto);
        assertEquals(response, dto);
    }

    @Test
    public void testInvalidateCache() {
        oemMappingService.invalidateCache();
    }

    @Test( expected = TBaseRuntimeException.class)
    public void testGetDependentGlAccounts_exception() {
        oemMappingService.getDependentGlAccounts(OEM.GM, 2021, 1, new HashSet<>(), 435454884L, null);
    }

    public void testGetDependentGlAccounts_success() {
        Long epoch = 935454884L;
        int year = TimeUtils.buildCalendar(epoch).get(Calendar.YEAR);
        oemMappingService.getDependentGlAccounts(OEM.GM, year, 1, new HashSet<>(), epoch, null);
    }

    @Test
    public void testGetOEMFsCellCodeSnapshot() {
        Set<String> codes = Sets.newHashSet();
        codes.add("_204");
        Mockito.when(oemFsCellCodeSnapshotRepo.findSnapshotByCodesAndMonth(Mockito.anyString(), Mockito.anyInt(), Mockito.anySet(), Mockito.anyString()))
                .thenReturn(oemFsCellCodeSnapshotList());
        oemMappingService.getOEMFsCellCodeSnapshot("-1-4", "GM", 1, 2019, 2,
                codes, 2019, true, true);
    }


    @Test
    public void testGetBulkOEMFsCellCodeSnapshot() {
        Set<String> codes = Sets.newHashSet();
        codes.add("_204");
        MonthInfo monthInfo = getMonthInfo();
        monthInfo.setYear(1960);
        Mockito.when(accountingService.getActiveMonthInfo())
                        .thenReturn(monthInfo);
        Mockito.when(oemFsCellCodeSnapshotRepo.getFsCellCodeByTimestamp(Mockito.anyLong(), Mockito.anyLong(), Mockito.anySet(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(oemFsCellCodeSnapshotList());
        oemMappingService.getBulkOEMFsCellCodeSnapshot("GM", codes, 999999923L, 999999923L, 1, 2021, true, "4", true);
    }

    @Test
    public void testUpdateSiteIdInOemMappings() {
        Mockito.when(oemFSMappingRepo.findMappingsForOEMYearVersionByDealerIdNonDeleted(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString()))
                        .thenReturn(oemFsMappingList());
        Mockito.when(fsEntryRepo.findByOemYearVersion(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString()))
                        .thenReturn(getFsEntry());
        Mockito.when(memoWorksheetRepo.findForOemByYearOemIdVersion(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
                        .thenReturn(getMemoWorksheetList());
        Mockito.when(oemFsCellCodeSnapshotRepo.findAllSnapshotByYearOemIdVersion(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString()))
                        .thenReturn(oemFsCellCodeSnapshotList());
        Mockito.when(hcWorksheetRepo.findByOemIdYearVersion(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
                        .thenReturn(getHCWorksheetList());
        Mockito.when(oemFsMappingSnapshotRepo.findAllSnapshotsByYearVersionOemId(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
                        .thenReturn(getOemFsMappingSnapshotList());
        oemMappingService.updateSiteIdInOemMappings(getOemFsMappingSiteIdChangesReqDtoList());
        Mockito.verify(oemFSMappingRepo, Mockito.times(1))
                .updateBulk(Mockito.any());
        Mockito.verify(fsEntryRepo, Mockito.times(1))
                .save(Mockito.any());
        Mockito.verify(memoWorksheetRepo, Mockito.times(1))
                .updateBulk(Mockito.any(), Mockito.anyString());
        Mockito.verify(hcWorksheetRepo, Mockito.times(1))
                .updateBulk(Mockito.anyList(), Mockito.anyString());
    }

    @Test
    public void testUpdateSiteIdInOemMappings_whenInputIsNull() {
        oemMappingService.updateSiteIdInOemMappings(null);
        Mockito.verify(oemFSMappingRepo, Mockito.times(0))
                .updateBulk(Mockito.any());
        Mockito.verify(fsEntryRepo, Mockito.times(0))
                .save(Mockito.any());
        Mockito.verify(memoWorksheetRepo, Mockito.times(0))
                .updateBulk(Mockito.any(), Mockito.anyString());
        Mockito.verify(hcWorksheetRepo, Mockito.times(0))
                .updateBulk(Mockito.anyList(), Mockito.anyString());
    }


    private List<GLAccount> glAccountList() {
        List<GLAccount> glAccountList = new ArrayList<>();
        GLAccount glAccount1 = new GLAccount();
        glAccount1.setDeleted(false);
        glAccount1.setAccountNumber("1234");
        glAccount1.setId("1");
        glAccount1.setAccountName("gl1");
        glAccount1.setDealerId("5");
        glAccount1.setAccountTypeId("EXPENSE_ALLOCATION");
        glAccountList.add(glAccount1);
        return glAccountList;
    }

    private FSCellCodeSnapshotDto getFsCellCodeSnapshotDto() {
        List<Integer> months = new ArrayList<>();
        months.add(11);
        months.add(12);
        FSCellCodeSnapshotDto dto = new FSCellCodeSnapshotDto();
        dto.setYear(2022);
        dto.setOemId("GM");
        dto.setSnapshotMonths(months);
        return dto;
    }

    private List<OemFsMappingSiteIdChangesReqDto> getOemFsMappingSiteIdChangesReqDtoList() {
        List<OemFsMappingSiteIdChangesReqDto> dtoList = new ArrayList<>();
        OemFsMappingSiteIdChangesReqDto dto1 = new OemFsMappingSiteIdChangesReqDto();
        dto1.setOemId("GM");
        dto1.setVersion(1);
        dto1.setYear(2021);
        dto1.setSiteId("-1_4");
        dtoList.add(dto1);

        OemFsMappingSiteIdChangesReqDto dto2 = new OemFsMappingSiteIdChangesReqDto();
        dto2.setOemId("Acura");
        dto2.setVersion(1);
        dto2.setYear(2021);
        dtoList.add(dto2);
        return dtoList;
    }

    private List<OEMFsCellCodeSnapshot> oemFsCellCodeSnapshotList() {
        OEMFsCellCodeSnapshot snapshot1 = new OEMFsCellCodeSnapshot();
        snapshot1.setValue(new BigDecimal(0));
        snapshot1.setOemId("GM");
        snapshot1.setMonth(1);
        snapshot1.setVersion(1);
        snapshot1.setDealerId("4");
        snapshot1.setMonth(2);

        List<OEMFsCellCodeSnapshot> snapshots = new ArrayList<>();
        snapshots.add(snapshot1);
        return snapshots;
    }

    private FSCellGroupCodesCreateDto getFSCellGroupCodesCreateDto() {
        FSCellGroupCodesCreateDto dto = new FSCellGroupCodesCreateDto();
        dto.setGroups(Arrays.asList(getFsCellGroupCodeCreateDto()));
        return dto;
    }

    private FSCellGroupCodeCreateDto getFsCellGroupCodeCreateDto() {
        FSCellGroupCodeCreateDto dto = new FSCellGroupCodeCreateDto();
        dto.setOemId(OEM.GM);
        dto.setYear(2021);
        dto.setGroupDisplayName("204");
        return dto;
    }

    private MappingSnapshotDto getMappingSnapshotDto(){
        MappingSnapshotDto dto = new MappingSnapshotDto();
        dto.setFsId("1234");
        dto.setMonth(2);
        dto.setSnapshotYear(2020);
        dto.setSnapshotMonths(Arrays.asList(1,2,3));
        return dto;
    }


    private  List<OemFsMapping> oemFsMappingList() {
        OemFsMapping oemFsMapping = OemFsMapping.builder().glAccountId("5_001").fsCellGroupCode("_204").fsId("617a3cf25f150300060e3b57").build();
        return Arrays.asList(oemFsMapping);
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

    private OemConfig getOemConfig()
    {
        OemConfig oemConfig=new OemConfig();
        oemConfig.setEnableRoundOffOffset(true);
        return oemConfig;
    }


    private List<AccountingOemFsCellGroup> getAccountingOemFsGroupCode() {
        List<AccountingOemFsCellGroup> list = new ArrayList<>();
        AccountingOemFsCellGroup accountingOemFsCellGroup = new AccountingOemFsCellGroup();
        accountingOemFsCellGroup.setOemId("GM");
        accountingOemFsCellGroup.setGroupCode("_204");
        accountingOemFsCellGroup.setGroupDisplayName("204");
        accountingOemFsCellGroup.setYear(2021);
        accountingOemFsCellGroup.setId("123");

        AccountingOemFsCellGroup accountingOemFsCellGroup2 = new AccountingOemFsCellGroup();
        accountingOemFsCellGroup2.setOemId("GM");
        accountingOemFsCellGroup2.setGroupCode("_215");
        accountingOemFsCellGroup2.setGroupDisplayName("215");
        accountingOemFsCellGroup2.setYear(2021);
        accountingOemFsCellGroup2.setId("124");
        list.add(accountingOemFsCellGroup);
        list.add(accountingOemFsCellGroup2);
        return list;
    }

    private OEMFinancialMappingMedia getFSMappingMedia() {
        List<MediaResponse> mediaResponses = new ArrayList<>();
        return OEMFinancialMappingMedia.builder()
                .dealerId("4")
                .fsId("9876-1234-5678-0987")
                .oemId("GM")
                .year("2021")
                .medias(mediaResponses)
                .build();
    }

    private List<OEMFinancialMapping> getOEMFinancialMappings() {
        List<OEMFinancialMapping> oemFinancialMappingList = new ArrayList<>();
        oemFinancialMappingList.add(OEMFinancialMapping.builder()
                .year("2021")
                .oemId("GM")
                .fsId("1234-5678-1234-5678")
                .glAccountDealerId("2002")
                .glAccountDealerId("4")
                .oemAccountNumber("_202")
                .dealerId("4")
                .siteId("_1-4")
                .build());
        return oemFinancialMappingList;
    }

    private AccountingSettings getAccountingSettings() {
        AccountingSettings accountingSettings = new AccountingSettings();
        accountingSettings.setFiscalYearStartMonth(1);
        return accountingSettings;
    }

    private MonthInfo getMonthInfo() {
        MonthInfo monthInfo = new MonthInfo();
        monthInfo.setMonth(3);
        monthInfo.setYear(2020);
        return monthInfo;
    }

    private TrialBalance getTrialBalance() {
        TrialBalance trialBalance = new TrialBalance();

        TrialBalanceRow trialBalanceRow1 = new TrialBalanceRow();
        trialBalanceRow1.setAccountId("ac1");
        trialBalanceRow1.setAccountType(AccountType.ASSET.name());
        trialBalanceRow1.setAccountNumber("1234");
        trialBalanceRow1.setCount(2l);
        trialBalanceRow1.setDebit(BigDecimal.TEN);
        trialBalanceRow1.setCredit(BigDecimal.ONE);
        trialBalanceRow1.setYtdCount(5l);
        trialBalanceRow1.setCurrentBalance(BigDecimal.ONE);
        trialBalanceRow1.setOpeningBalance(BigDecimal.ZERO);

        TrialBalanceRow trialBalanceRow2 = new TrialBalanceRow();
        trialBalanceRow2.setAccountId("ac2");
        trialBalanceRow2.setAccountType(AccountType.EQUITY.name());
        trialBalanceRow2.setAccountNumber("34");
        trialBalanceRow2.setCount(3l);
        trialBalanceRow2.setDebit(BigDecimal.ONE);
        trialBalanceRow2.setCredit(BigDecimal.TEN);
        trialBalanceRow2.setYtdCount(6l);
        trialBalanceRow2.setCurrentBalance(BigDecimal.ZERO);
        trialBalanceRow2.setOpeningBalance(BigDecimal.ZERO);

        trialBalance.setAccountRows(Stream.of(trialBalanceRow1, trialBalanceRow2).collect(Collectors.toList()));
        return trialBalance;
    }

    private List<AccountingOemFsCellCode> getAccountingOemFsCellCodes() {
        Map<String, String> additionalInfo = Maps.newHashMap();
        additionalInfo.put("month", "10");
        additionalInfo.put("minuendCellCode", "psPage");
        AccountingOemFsCellCode accountingOemFsCellCode1 = AccountingOemFsCellCode.builder()
                .oemCode(OEM.GM.getOem())
                .groupCode("_203")
                .oemId(OEM.GM.getOem())
                .derived(false)
                .displayName("display")
                .subType(OemCellSubType.MONTHLY.name())
                .year(2020)
                .version(1)
                .code("203")
                .additionalInfo(additionalInfo)
                .source(FsCellCodeSource.ROUNDOFF_OFFSET)
                .valueType("BALANCE")
                .subType("mtd")
                .build();

        List<String> dependentCellCode = Arrays.asList("203");
        Map<String, String> additionalInfo1 = Maps.newHashMap();
        additionalInfo1.put("month", "10");
        AccountingOemFsCellCode accountingOemFsCellCode2 = AccountingOemFsCellCode.builder()
                .oemCode(OEM.GM.getOem())
                .groupCode("code2")
                .oemId(OEM.GM.getOem())
                .derived(false)
                .displayName("display1")
                .subType(OemCellSubType.MONTHLY.name())
                .year(2021)
                .version(2)
                .valueType("COUNT")
                .dependentFsCellCodes(dependentCellCode)
                .derived(true)
                .code("204")
                .expression("20A + 30A")
                .additionalInfo(additionalInfo1)
                .build();

        Map<String, String> additionalInfo2 = Maps.newHashMap();
        additionalInfo2.put("month", "2");
        AccountingOemFsCellCode accountingOemFsCellCode3 = AccountingOemFsCellCode.builder()
                .oemCode(OEM.GM.getOem())
                .groupCode("_205")
                .oemId(OEM.GM.getOem())
                .derived(false)
                .displayName("205")
                .subType(OemCellSubType.MONTHLY.name())
                .year(2021)
                .version(2)
                .valueType("COUNT")
                .derived(true)
                .code("205")
                .dependentFsCellCodes(dependentCellCode)
                .expression("25A + 40D")
                .additionalInfo(additionalInfo2)
                .build();

        return Stream.of(accountingOemFsCellCode1, accountingOemFsCellCode2, accountingOemFsCellCode3).collect(Collectors.toList());
    }

    private List<AccountingOemFsCellCode> getAccountingOemFsCellCodes_memoWorksheet() {
        Map<String, String> additionalInfo = Maps.newHashMap();
        additionalInfo.put("month", "10");
        additionalInfo.put("minuendCellCode", "psPage");
        additionalInfo.put("memoKey", "memoKey");
        additionalInfo.put("sourceType", "random");
        AccountingOemFsCellCode accountingOemFsCellCode1 = AccountingOemFsCellCode.builder()
                .oemCode(OEM.GM.getOem())
                .groupCode("_203")
                .oemId(OEM.GM.getOem())
                .derived(false)
                .displayName("display")
                .subType(OemCellSubType.MONTHLY.name())
                .year(2021)
                .version(1)
                .code("203")
                .additionalInfo(additionalInfo)
                .source(FsCellCodeSource.MEMO_WRKSHT)
                .valueType("BALANCE")
                .subType("mtd")
                .build();

        List<String> dependentCellCode = Arrays.asList("203");
        Map<String, String> additionalInfo1 = Maps.newHashMap();
        additionalInfo1.put("month", "10");

        return Stream.of(accountingOemFsCellCode1).collect(Collectors.toList());
    }

    private List<AccountingOemFsCellCode> getAccountingOemFsCellCodes_hcWorksheet() {
        Map<String, String> additionalInfo = Maps.newHashMap();
        additionalInfo.put("month", "10");
        additionalInfo.put("minuendCellCode", "psPage");
        additionalInfo.put("department", "department");
        additionalInfo.put("position", "position");
        AccountingOemFsCellCode accountingOemFsCellCode1 = AccountingOemFsCellCode.builder()
                .oemCode(OEM.GM.getOem())
                .groupCode("_203")
                .oemId(OEM.GM.getOem())
                .derived(false)
                .displayName("display")
                .subType(OemCellSubType.MONTHLY.name())
                .year(2021)
                .version(1)
                .code("203")
                .additionalInfo(additionalInfo)
                .source(FsCellCodeSource.HEADCOUNT_WRKSHT)
                .valueType("BALANCE")
                .subType("mtd")
                .build();

        List<String> dependentCellCode = Arrays.asList("203");
        Map<String, String> additionalInfo1 = Maps.newHashMap();
        additionalInfo1.put("month", "10");

        return Stream.of(accountingOemFsCellCode1).collect(Collectors.toList());
    }

    private List<OemFsMappingSnapshot> getOemFsMappingSnapshotList() {
        OemFsMappingSnapshot oemFsMappingSnapshot1 = new OemFsMappingSnapshot();
        oemFsMappingSnapshot1.setDealerId("4");
        oemFsMappingSnapshot1.setMonth(1);
        oemFsMappingSnapshot1.setGlAccountId("ac1");
        oemFsMappingSnapshot1.setOemId(OEM.GM.getOem());
        oemFsMappingSnapshot1.setVersion(1);
        oemFsMappingSnapshot1.setYear(2020);
        oemFsMappingSnapshot1.setFsCellGroupCode("_204");

        OemFsMappingSnapshot oemFsMappingSnapshot2 = new OemFsMappingSnapshot();
        oemFsMappingSnapshot2.setDealerId("4");
        oemFsMappingSnapshot2.setMonth(2);
        oemFsMappingSnapshot2.setGlAccountId("ac1");
        oemFsMappingSnapshot2.setOemId(OEM.GM.getOem());
        oemFsMappingSnapshot2.setVersion(1);
        oemFsMappingSnapshot2.setYear(2019);
        oemFsMappingSnapshot2.setFsCellGroupCode("_205");

        return Stream.of(oemFsMappingSnapshot1, oemFsMappingSnapshot2).collect(Collectors.toList());
    }

    private MemoWorksheet getMemoWorksheet1() {
        MemoWorksheet memoWorksheet = new MemoWorksheet();

        memoWorksheet.setId("id1");
        memoWorksheet.setDealerId("4");
        memoWorksheet.setKey("memoKey");
        memoWorksheet.setOemId(OEM.GM.getOem());
        memoWorksheet.setFieldType(FieldType.COUNT.name());
        memoWorksheet.setVersion(1);
        memoWorksheet.setYear(2020);
        memoWorksheet.setCreatedByUserId("user1");
        memoWorksheet.setModifiedByUserId("user2");

        MemoValue memoValue1 = new MemoValue();
        memoValue1.setMonth(1);
        memoValue1.setMtdValue(BigDecimal.ZERO);
        memoValue1.setYtdValue(BigDecimal.ONE);

        MemoValue memoValue2 = new MemoValue();
        memoValue2.setMonth(2);
        memoValue2.setMtdValue(BigDecimal.ONE);
        memoValue2.setYtdValue(BigDecimal.TEN);

        memoWorksheet.setValues(Stream.of(memoValue1, memoValue2).collect(Collectors.toList()));

        return memoWorksheet;
    }

    private MemoWorksheet getMemoWorksheet2() {
        MemoWorksheet memoWorksheet = new MemoWorksheet();

        memoWorksheet.setId("id2");
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

        memoWorksheet.setValues(Stream.of(memoValue1, memoValue2).collect(Collectors.toList()));

        return memoWorksheet;
    }

    private List<MemoWorksheet> getMemoWorksheetList() {
        return Stream.of(getMemoWorksheet1(), getMemoWorksheet2()).collect(Collectors.toList());
    }

    private HCWorksheet getHCWorksheet1() {
        HCWorksheet hcWorksheet = new HCWorksheet();

        hcWorksheet.setCreatedByUserId("user1");
        hcWorksheet.setModifiedByUserId("user2");
        hcWorksheet.setDealerId("4");
        hcWorksheet.setDepartment("department");
        hcWorksheet.setOemId(OEM.GM.getOem());
        hcWorksheet.setVersion(2);
        hcWorksheet.setYear(2020);
        hcWorksheet.setPosition("position");

        HCValue hcValue1 = new HCValue();
        hcValue1.setMonth(1);
        hcValue1.setValue(BigDecimal.ZERO);

        HCValue hcValue2 = new HCValue();
        hcValue2.setMonth(2);
        hcValue2.setValue(BigDecimal.ZERO);

        hcWorksheet.setValues(Stream.of(hcValue1, hcValue2).collect(Collectors.toList()));

        return hcWorksheet;
    }

    private HCWorksheet getHCWorksheet2() {
        HCWorksheet hcWorksheet = new HCWorksheet();
        hcWorksheet.setCreatedByUserId("user3");
        hcWorksheet.setModifiedByUserId("user4");
        hcWorksheet.setDealerId("4");
        hcWorksheet.setDepartment("department");
        hcWorksheet.setOemId(OEM.GM.getOem());
        hcWorksheet.setVersion(3);
        hcWorksheet.setYear(2019);
        hcWorksheet.setPosition("position");

        HCValue hcValue1 = new HCValue();
        hcValue1.setMonth(1);
        hcValue1.setValue(BigDecimal.ZERO);

        HCValue hcValue2 = new HCValue();
        hcValue2.setMonth(2);
        hcValue2.setValue(BigDecimal.ZERO);

        hcWorksheet.setValues(Stream.of(hcValue1, hcValue2).collect(Collectors.toList()));

        return hcWorksheet;
    }

    private List<HCWorksheet> getHCWorksheetList() {
        return Stream.of(getHCWorksheet1(), getHCWorksheet2()).collect(Collectors.toList());
    }

    private OemFsMappingUpdateDto oemFsMappingUpdateDtoRequest() {
        OemFsMappingUpdateDto oemFsMappingUpdateDto = new OemFsMappingUpdateDto();
        oemFsMappingUpdateDto.setFsId("617a3cf25f150300060e3b57");

        OemFsMappingDetail oemFsMappingDetail1 = new OemFsMappingDetail();
        oemFsMappingDetail1.setGlAccountId("5_001");
        oemFsMappingDetail1.setFsCellGroupCode("_202");

        OemFsMappingDetail oemFsMappingDetail2 = new OemFsMappingDetail();
        oemFsMappingDetail2.setGlAccountId("5_001");
        oemFsMappingDetail2.setFsCellGroupCode("_202");

        OemFsMappingDetail oemFsMappingDetail3 = new OemFsMappingDetail();
        oemFsMappingDetail3.setGlAccountId("5_001");
        oemFsMappingDetail3.setFsCellGroupCode("_203");

        OemFsMappingDetail oemFsMappingDetail4 = new OemFsMappingDetail();
        oemFsMappingDetail4.setGlAccountId("5_001");
        oemFsMappingDetail4.setFsCellGroupCode("_206");

        OemFsMappingDetail oemFsMappingDetail5 = new OemFsMappingDetail();
        oemFsMappingDetail5.setGlAccountId("5_001");
        oemFsMappingDetail5.setFsCellGroupCode("_207");
        oemFsMappingUpdateDto.setMappingsToSave(Arrays.asList(oemFsMappingDetail1, oemFsMappingDetail2, oemFsMappingDetail3));
        oemFsMappingUpdateDto.setMappingsToDelete(Arrays.asList(oemFsMappingDetail4, oemFsMappingDetail5));
        return  oemFsMappingUpdateDto;
    }

    private OemMappingRequestDto getOemMappingRequestDto() {
        OemMappingRequestDto oemMappingRequestDto = new OemMappingRequestDto();
        oemMappingRequestDto.setDealerId("4");
        oemMappingRequestDto.setFsId("1234-568");
        oemMappingRequestDto.setMedias(new ArrayList<>());

        OEMMappingDto oemMappingDto = new OEMMappingDto();
        oemMappingDto.setId("4567");
        oemMappingDto.setOemAccountNumber("123456");
        oemMappingDto.setGlAccountId("202");
        oemMappingDto.setGlAccountDealerId("44");

        OemMappingRequestDto oemMappingRequestDto2 = new OemMappingRequestDto();
        oemMappingRequestDto2.setDealerId("4");
        oemMappingRequestDto2.setFsId("1234-568");
        oemMappingRequestDto2.setMedias(new ArrayList<>());

        OEMMappingDto oemMappingDto2 = new OEMMappingDto();
        oemMappingDto2.setId("4567");
        oemMappingDto2.setGlAccountId("202");
        oemMappingDto2.setGlAccountDealerId("44");

        oemMappingRequestDto.setMappings(Arrays.asList(oemMappingDto, oemMappingDto2));
        return oemMappingRequestDto;
    }

    private List<FSEntry> getFsEntries() {
        FSEntry fsEntry1 = getFsEntry();
        fsEntry1.setFsType(FSType.OEM.name());
        FSEntry fsEntry2 = getFsEntry();
        fsEntry2.setYear(2022);
        fsEntry2.setFsType(FSType.INTERNAL.name());
        List<FSEntry> fsEntries = new ArrayList<>();
        fsEntries.add(fsEntry1);
        fsEntries.add(fsEntry2);
        return fsEntries;
    }

}