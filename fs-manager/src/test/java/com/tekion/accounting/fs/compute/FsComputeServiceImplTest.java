package com.tekion.accounting.fs.compute;

import com.google.common.cache.LoadingCache;
import com.tekion.accounting.fs.beans.common.AccountingOemFsCellCode;
import com.tekion.accounting.fs.beans.mappings.OemFsMappingSnapshot;
import com.tekion.accounting.fs.beans.memo.*;
import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.accounting.fs.common.utils.TimeUtils;
import com.tekion.accounting.fs.dto.cellcode.FsCellCodeDetailsResponseDto;
import com.tekion.accounting.fs.enums.AccountType;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.repos.FSCellCodeRepo;
import com.tekion.accounting.fs.repos.OemConfigRepo;
import com.tekion.accounting.fs.repos.OemFsMappingSnapshotRepo;
import com.tekion.accounting.fs.repos.worksheet.HCWorksheetRepo;
import com.tekion.accounting.fs.repos.worksheet.MemoWorksheetRepo;
import com.tekion.accounting.fs.service.accountingService.AccountingService;
import com.tekion.accounting.fs.service.compute.FsComputeServiceImpl;
import com.tekion.accounting.fs.service.compute.models.CellCodeKey;
import com.tekion.as.models.beans.AccountingSettings;
import com.tekion.as.models.beans.TrialBalance;
import com.tekion.as.models.beans.TrialBalanceRow;
import com.tekion.as.models.dto.MonthInfo;
import com.tekion.beans.DynamicProperty;
import com.tekion.core.utils.UserContext;
import com.tekion.core.utils.UserContextProvider;
import com.tekion.propertyclient.DPClient;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.tekion.accounting.fs.common.TConstants.ACCOUNTING_MODULE;
import static org.mockito.ArgumentMatchers.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class FsComputeServiceImplTest extends TestCase {

	private static final String FS_ROUND_OFF_PROPERTY = "FS_USE_PRECISION";

	@InjectMocks
	FsComputeServiceImpl oemMappingService;

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
	DynamicProperty<String> dealerRoundOffPref;
	@Mock
	LoadingCache<CellCodeKey, List<AccountingOemFsCellCode>> accountingOemFsCellCodesCache;
	@Mock
	OemConfigRepo oemConfigRepo;
	@Mock
	AccountingService accountingService;

	@Before
	public void setUp() throws ExecutionException {
		UserContextProvider.setContext(new UserContext("-1", "ca", "4"));
		Mockito.when(dealerConfig.getDealerTimeZone()).thenReturn(TimeZone.getTimeZone("America/Los_Angeles"));
		Mockito.when(fsCellCodeRepo.getFsCellCodesForOemYearAndCountry(anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString())).
				thenReturn(getAccountingOemFsCellCodes());
		//oemMappingService.postConstruct();
		Mockito.when(dpClient.getStringProperty(ACCOUNTING_MODULE, FS_ROUND_OFF_PROPERTY)).
				thenReturn(dealerRoundOffPref);
		Mockito.when(accountingOemFsCellCodesCache.get(any(CellCodeKey.class))).thenReturn(new ArrayList<>());
		Mockito.when(oemConfigRepo.findByOemId(anyString(), anyString())).thenReturn(null);
	}


	@Test
	public void computeFsCellCodeDetails() {
		Mockito.when(accountingService.getActiveMonthInfo()).
				thenReturn(getMonthInfo());
		Mockito.when(accountingService.getTrialBalanceReportForMonth(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyLong(), Mockito.anySet(),
				Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyBoolean())).
				thenReturn(getTrialBalance());
		Mockito.when(oemFsMappingSnapshotRepo.findAllSnapshotByYearAndMonth(anyString(), Mockito.anyInt(), anyString())).
				thenReturn(getOemFsMappingSnapshotList());
		Mockito.when(memoWorksheetRepo.findForOemByYearOptimized(anyString(), anyString())).
				thenReturn(getMemoWorksheetList());
		Mockito.when(hcWorksheetRepo.findByFsId(anyString()))
				.thenReturn(getHCWorksheetList());
		Mockito.when(accountingService.getGlBalCntInfoForFS(Mockito.any()))
				.thenReturn(new HashMap<Integer, Map<String, Map<String, BigDecimal>>>() {
				});
		FsCellCodeDetailsResponseDto fsCellCodeDetailsResponseDto = oemMappingService.computeFsCellCodeDetailsByFsId("1", 12345, false, false);
		assertNotNull(fsCellCodeDetailsResponseDto);
	}

	@Test
	public void computeFsCellCodeDetailsWithEpoch() {
		Mockito.when(accountingService.getActiveMonthInfo()).
				thenReturn(getMonthInfo());
		Mockito.when(dealerRoundOffPref.getSafeGlobalValue(anyString())).
				thenReturn("");
		Mockito.when(dealerRoundOffPref.getSafeValueWithUserContext(anyString())).
				thenReturn("");
		Mockito.when(accountingService.getCYTrialBalanceTillDayOfMonth(Mockito.anyLong(), anySet(), anyBoolean(), anyBoolean(), anyBoolean(), false)).
				thenReturn(getTrialBalance());
		Mockito.when(oemFsMappingSnapshotRepo.findAllSnapshotByYearAndMonth(anyString(), Mockito.anyInt(), anyString())).
				thenReturn(getOemFsMappingSnapshotList());
		Mockito.when(memoWorksheetRepo.findForOemByYearOptimized(anyString(), anyString())).
				thenReturn(getMemoWorksheetList());
		Mockito.when(hcWorksheetRepo.findByFsId(anyString()))
				.thenReturn(getHCWorksheetList());
		Mockito.when(accountingService.getGlBalCntInfoForFS(Mockito.any()))
				.thenReturn(new HashMap<Integer, Map<String, Map<String, BigDecimal>>>() {
				});
		FsCellCodeDetailsResponseDto fsCellCodeDetailsResponseDto = oemMappingService.computeFsCellCodeDetailsByFsId("2", 1599202256l, false, false);
		assertNotNull(fsCellCodeDetailsResponseDto);
	}

	@Test
	public void computeFsCellCodeDetailsForFS() {
		Mockito.when(accountingService.getActiveMonthInfo()).
				thenReturn(getMonthInfo());
		Mockito.when(accountingService.getAccountingSettings()).
				thenReturn(getAccountingSettings());
		Mockito.when(oemFsMappingSnapshotRepo.findAllSnapshotByYearAndMonth(anyString(), Mockito.anyInt(), anyString())).
				thenReturn(getOemFsMappingSnapshotList());
		Mockito.when(accountingService.getFSTrialBalanceTillDayOfMonth(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyLong())).
				thenReturn(getTrialBalance());
		FsCellCodeDetailsResponseDto fsCellCodeDetailsResponseDto = oemMappingService.computeFsCellCodeDetailsForFS("", 1599202256l, false);
		assertNotNull(fsCellCodeDetailsResponseDto);
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
		AccountingOemFsCellCode accountingOemFsCellCode1 = AccountingOemFsCellCode.builder()
				.oemCode(OEM.GM.getOem())
				.groupCode("code1")
				.oemId(OEM.GM.getOem())
				.derived(false)
				.displayName("display")
				.year(2020)
				.version(1)
				.valueType("BALANCE")
				.subType("mtd")
				.build();

		AccountingOemFsCellCode accountingOemFsCellCode2 = AccountingOemFsCellCode.builder()
				.oemCode(OEM.GM.getOem())
				.groupCode("code2")
				.oemId(OEM.GM.getOem())
				.derived(false)
				.displayName("display1")
				.year(2021)
				.version(2)
				.valueType("COUNT")
				.subType("mtd")
				.build();

		return Stream.of(accountingOemFsCellCode1, accountingOemFsCellCode2).collect(Collectors.toList());
	}

	private List<OemFsMappingSnapshot> getOemFsMappingSnapshotList() {
		OemFsMappingSnapshot oemFsMappingSnapshot1 = new OemFsMappingSnapshot();
		oemFsMappingSnapshot1.setDealerId("4");
		oemFsMappingSnapshot1.setMonth(1);
		oemFsMappingSnapshot1.setGlAccountId("ac1");
		oemFsMappingSnapshot1.setOemId(OEM.GM.getOem());
		oemFsMappingSnapshot1.setVersion(1);
		oemFsMappingSnapshot1.setYear(2020);

		OemFsMappingSnapshot oemFsMappingSnapshot2 = new OemFsMappingSnapshot();
		oemFsMappingSnapshot2.setDealerId("4");
		oemFsMappingSnapshot2.setMonth(2);
		oemFsMappingSnapshot2.setGlAccountId("ac1");
		oemFsMappingSnapshot2.setOemId(OEM.GM.getOem());
		oemFsMappingSnapshot2.setVersion(1);
		oemFsMappingSnapshot2.setYear(2019);

		return Stream.of(oemFsMappingSnapshot1, oemFsMappingSnapshot2).collect(Collectors.toList());
	}

	private MemoWorksheet getMemoWorksheet1() {
		MemoWorksheet memoWorksheet = new MemoWorksheet();

		memoWorksheet.setId("id1");
		memoWorksheet.setDealerId("4");
		memoWorksheet.setKey("key1");
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

		hcWorksheet.setValues(Stream.of(hcValue1, hcValue2).collect(Collectors.toList()));

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

		hcWorksheet.setValues(Stream.of(hcValue1, hcValue2).collect(Collectors.toList()));

		return hcWorksheet;
	}

	private List<HCWorksheet> getHCWorksheetList() {
		return Stream.of(getHCWorksheet1(), getHCWorksheet2()).collect(Collectors.toList());
	}


}
