package com.tekion.accounting.fs.service.compute;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tekion.accounting.fs.auditevents.AccountingOemFsCellGroupAuditEvent;
import com.tekion.accounting.fs.auditevents.PclCodesAuditEventHelper;
import com.tekion.accounting.fs.beans.accountingInfo.AccountingInfo;
import com.tekion.accounting.fs.beans.common.*;
import com.tekion.accounting.fs.beans.mappings.*;
import com.tekion.accounting.fs.beans.memo.FieldType;
import com.tekion.accounting.fs.beans.memo.HCWorksheet;
import com.tekion.accounting.fs.beans.memo.MemoValue;
import com.tekion.accounting.fs.beans.memo.MemoWorksheet;
import com.tekion.accounting.fs.common.GlobalService;
import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.common.dpProvider.DpUtils;
import com.tekion.accounting.fs.common.exceptions.FSError;
import com.tekion.accounting.fs.common.utils.*;
import com.tekion.accounting.fs.dto.cellGrouop.FSCellGroupCodeCreateDto;
import com.tekion.accounting.fs.dto.cellGrouop.FSCellGroupCodesCreateDto;
import com.tekion.accounting.fs.dto.cellGrouop.FsGroupCodeDetail;
import com.tekion.accounting.fs.dto.cellGrouop.FsGroupCodeDetailsResponseDto;
import com.tekion.accounting.fs.dto.cellcode.*;
import com.tekion.accounting.fs.dto.context.FsReportContext;
import com.tekion.accounting.fs.dto.fsEntry.FsEntryCreateDto;
import com.tekion.accounting.fs.dto.mappings.*;
import com.tekion.accounting.fs.dto.oemConfig.OemConfigRequestDto;
import com.tekion.accounting.fs.dto.oemTemplate.OemTemplateReqDto;
import com.tekion.accounting.fs.dto.oemTemplate.TemplateDetail;
import com.tekion.accounting.fs.enums.*;
import com.tekion.accounting.fs.repos.*;
import com.tekion.accounting.fs.repos.worksheet.HCWorksheetRepo;
import com.tekion.accounting.fs.repos.worksheet.MemoWorksheetRepo;
import com.tekion.accounting.fs.service.accountingInfo.AccountingInfoService;
import com.tekion.accounting.fs.service.accountingService.AccountingService;
import com.tekion.accounting.fs.service.common.cache.CustomFieldConfig;
import com.tekion.accounting.fs.service.compute.models.CellCodeKey;
import com.tekion.accounting.fs.service.compute.models.OemFsCellContext;
import com.tekion.accounting.fs.service.fsEntry.FsEntryService;
import com.tekion.accounting.fs.service.tasks.ConsolidatedFsGlBalanceReportInEpochTask;
import com.tekion.accounting.fs.service.utils.FinancialStatementUtils;
import com.tekion.as.client.AccountingClient;
import com.tekion.as.models.beans.GLAccount;
import com.tekion.as.models.beans.MediaResponse;
import com.tekion.as.models.beans.TrialBalance;
import com.tekion.as.models.beans.TrialBalanceRow;
import com.tekion.as.models.beans.fs.FsReportDto;
import com.tekion.as.models.dto.MonthInfo;
import com.tekion.audit.client.manager.AuditEventManager;
import com.tekion.audit.client.manager.impl.AuditEventDTO;
import com.tekion.beans.DynamicProperty;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.TStringUtils;
import com.tekion.core.utils.UserContextProvider;
import com.tekion.core.validation.TValidator;
import com.tekion.propertyclient.DPClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Month;
import java.time.YearMonth;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.tekion.accounting.fs.beans.common.AccountingOemFsCellCode.additionInfoField_codeIdentifier;
import static com.tekion.accounting.fs.beans.common.AccountingOemFsCellCode.additionInfoField_month;
import static com.tekion.accounting.fs.common.AsyncContextDecorator.ASYNC_THREAD_POOL;
import static com.tekion.accounting.fs.common.TConstants.ACCOUNTING_MODULE;
import static com.tekion.core.utils.UserContextProvider.getCurrentDealerId;
import static com.tekion.core.utils.UserContextProvider.getCurrentTenantId;
import static java.util.stream.Collectors.groupingBy;

@Component
@Slf4j
@RequiredArgsConstructor
public class FsComputeServiceImpl implements FsComputeService {
	private final OEMFinancialMappingRepository oemFSMappingRepo;
	private final OEMFinancialMappingMediaRepository oemFSMediaRepo;
	private final TValidator validator;
	private final OemFSMappingRepo oemFsMappingRepo;
	private final FSCellCodeRepo fsCellCodeRepo;
	private final OemTemplateRepo oemTemplateRepo;
	private final OemFsCellGroupRepo oemFsCellGroupRepo;
	private final MemoWorksheetRepo memoWorksheetRepo;
	private final HCWorksheetRepo hcWorksheetRepo;
	private final OEMFsCellCodeSnapshotRepo oemFsCellCodeSnapshotRepo;
	private final OemFsMappingSnapshotRepo oemFsMappingSnapshotRepo;
	private final DealerConfig dealerConfig;
	private final OemConfigRepo oemConfigRepo;
	private final AccountingInfoService aiService;
	private final DPClient dpClient;
	public final FsEntryService fsEntryService;
	public final FSEntryRepo fsEntryRepo;
	public final GlobalService globalService;
	public final AccountingClient accountingClient;
	public final AccountingService accountingService;
	public final CustomFieldConfig customFieldConfig;
	private final AuditEventManager auditEventManager;


	@Qualifier(ASYNC_THREAD_POOL)
	@Autowired
	private AsyncTaskExecutor executorService;

	private LoadingCache<CellCodeKey, List<AccountingOemFsCellCode>> accountingOemFsCellCodesCache;
	private static final String DEFAULT_ROUND_OFF = "";
	private static final String ENABLE_ROUND_OFF = "YES";
	private static final String DISABLE_ROUND_OFF = "NO";
	private static final String FS_ROUND_OFF_PROPERTY = "FS_USE_PRECISION";
	private static final String OFFSET_VALUE = "offset";
	private static final String ORIGINAL_VALUE = "original";
	private static final String ROUNDED_VALUE = "rounded";
	private static final String SOURCE_TYPE = "sourceType";
	public static final String NCT_DATE_FORMAT = "dd-MMM-yy";

	private DynamicProperty<String> dealerRoundOffPref;

	private final int defaultMonth = 0;
	public static final int FIRST_RECORD = 0;

	@PostConstruct
	public void postConstruct(){
		accountingOemFsCellCodesCache = CacheBuilder.newBuilder()
				.expireAfterWrite(30, TimeUnit.MINUTES).maximumSize(5)
				.build(new CacheLoader<CellCodeKey, List<AccountingOemFsCellCode>>() {
					@Override
					public List<AccountingOemFsCellCode> load(CellCodeKey key) {
						return getOemTMappingList(key.getOemId(), key.getYear(),key.getVersion(), key.getCountryCode());
					}
				});
		dealerRoundOffPref = dpClient.getStringProperty(ACCOUNTING_MODULE, FS_ROUND_OFF_PROPERTY);
	}

	@Override
	public void saveMapping(OemMappingRequestDto mappingRequest) {
		validator.validate(mappingRequest);
		List<OEMMappingDto> mappings= TCollectionUtils
				.nullSafeList(mappingRequest.getMappings());

		List<OEMFinancialMapping> mappingsToDelete = mappings.stream().filter(m -> {
			return m.getId() != null && m.getOemAccountNumber() == null;
		}).map(m -> fromDto(m, mappingRequest)).collect(Collectors.toList());

		List<OEMFinancialMapping> mappingsToUpdate = mappings.stream().filter(m -> {
			return m.getOemAccountNumber() != null;
		}).map(mapping -> fromDto(mapping, mappingRequest)).collect(Collectors.toList());
		if(TCollectionUtils.isNotEmpty(mappingsToDelete)){
			oemFSMappingRepo.deleteMappings(mappingsToDelete);
		}

		if(TCollectionUtils.isNotEmpty(mappingsToUpdate)) {
			oemFSMappingRepo.upsertMappings(mappingsToUpdate);
		}

		FSEntry fsEntry = fsEntryRepo.findByIdAndDealerIdWithNullCheck(mappingRequest.getFsId(), mappingRequest.getDealerId());
		if(mappingRequest.getMedias() != null){
			OEMFinancialMappingMedia oemMappingMedia = OEMFinancialMappingMedia.builder()
					.dealerId(mappingRequest.getDealerId())
					.medias(mappingRequest.getMedias())
					.fsId(mappingRequest.getFsId()).build();

			OEMFinancialMappingMedia savedMedia = oemFSMediaRepo
					.findSavedMediaByDealerIdNonDeleted(fsEntry.getOemId(), fsEntry.getYear().toString(), mappingRequest.getDealerId());
			if(savedMedia!=null){
				oemMappingMedia.setId(savedMedia.getId());
			}
			oemFSMediaRepo.saveMedia(oemMappingMedia);
		}
	}

	@Override
	public OEMMappingResponse getOEMMappingByDealerId(OEM oem, String dealerId, String year) {
		OEMFinancialMappingMedia savedMedia = oemFSMediaRepo
				.findSavedMediaByDealerIdNonDeleted(oem.name(), year, dealerId);
		List<MediaResponse> medias = null;
		if(savedMedia != null){
			medias = savedMedia.getMedias();
		}

		List<OEMFinancialMapping> mappings = oemFSMappingRepo
				.findMappingsByFsIdAndDealerId(year, getCurrentDealerId());
		return OEMMappingResponse.builder()
				.mappings(mappings)
				.medias(medias)
				.build();
	}

	@Override
	public List<AccountingOemFsCellCode> getOemTMappingList(String oemId, Integer year, Integer version, String countryCode) {
		log.info("Fetching AccountingOemFsCellCode from DB.");
		return fsCellCodeRepo.getFsCellCodesForOemYearAndCountry(oemId, year, version, countryCode);
	}

	@Override
	public List<AccountingOemFsCellCode> getOemTMappingList(String oemId, Integer year, Integer version, String countryCode, boolean readFromCache){
		if(StringUtils.isEmpty(countryCode)){
			countryCode = dealerConfig.getDealerCountryCode();
		}
		if(readFromCache){
			log.info("callToRead cell codes from cache!");
			return getOemTMappingListFromCache(oemId, year, version, countryCode);
		}
		return getOemTMappingList(oemId, year, version, countryCode);
	}

	private List<AccountingOemFsCellCode> getOemTMappingListFromCache(String oemId, Integer year, Integer version, String countryCode) {
		CellCodeKey cellCodeKey = CellCodeKey.builder().oemId(oemId).year(year).version(version).countryCode(countryCode).build();
		try {
			return accountingOemFsCellCodesCache.get(cellCodeKey);
		} catch (ExecutionException e) {
			log.error("Error while fetching cell codes from cache ",e);
			return getOemTMappingList(oemId,year,version, countryCode);
		}
	}

	@Override
	public FsCellCodeDetailsResponseDto computeFsCellCodeDetails(String oemId, Integer oemFsYear, Integer version, Integer year, Integer month, boolean includeM13, String siteId, boolean addM13BalInDecBalances) {
		FSEntry fsEntry = fsEntryRepo.findDefaultType(oemId, oemFsYear ,UserContextProvider.getCurrentDealerId(), UserContextUtils.getSiteIdFromUserContext());
		MonthInfo activeMonthInfo = getActiveMonthInfo();

		if(Objects.nonNull(year) && Objects.nonNull(month) && isFutureMonth(activeMonthInfo, year, month - 1)){
			return FsCellCodeDetailsResponseDto.builder().build();
		}
		Map<String, TrialBalanceRow> trialBalanceRowMap = getTrialBalanceRowForGlAccounts(activeMonthInfo, year, month, includeM13, addM13BalInDecBalances);

		FsReportContext context = FsReportContext.builder()
				.fsId(fsEntry.getId())
				.fromMonth(Calendar.JANUARY + 1)
				.fromYear(year)
				.oemFsYear(oemFsYear)
				.oemId(oemId)
				.requestedMonth(month)
				.requestedYear(year)
				.siteId(siteId)
				.trialBalanceRowMap(trialBalanceRowMap)
				.version(version)
				.fsByFiscalYear(false)
				.fsTime(TimeUtils.getMonthsEndTime(year, month))
				.build();

		return getFsCellCodeDetailsReport(context);
	}

	@Override
	public FsCellCodeDetailsResponseDto computeFsCellCodeDetailsByFsId(String fsId, long tillEpoch, boolean includeM13, boolean addM13BalInDecBalances){
		FSEntry fsEntry =fsEntryRepo.findByIdAndDealerIdWithNullCheck(fsId, UserContextProvider.getCurrentDealerId());
		return computeFsCellCodeDetails(fsEntry, tillEpoch, includeM13, addM13BalInDecBalances);
	}

	@Override
	public FsCellCodeDetailsResponseDto computeFsCellCodeDetails(FSEntry fsEntry, long tillEpoch, boolean includeM13, boolean addM13BalInDecBalances) {
		MonthInfo activeMonthInfo = getActiveMonthInfo();
		Calendar c = TimeUtils.buildCalendar(tillEpoch);
		if(isFutureMonth(activeMonthInfo, c.get(Calendar.YEAR), c.get(Calendar.MONTH) )){
			return FsCellCodeDetailsResponseDto.builder().build();
		}
		log.info("includeM13: {}", includeM13);
		AccountingInfo accountingInfo = aiService.find(UserContextProvider.getCurrentDealerId());
		OemConfig oemConfig = getOemConfig(fsEntry.getOemId());

		FsReportContext context = FsReportContext.builder()
				.fsId(fsEntry.getId())
				.fromMonth(Calendar.JANUARY+1)
				.fromYear(c.get(Calendar.YEAR))
				.oemFsYear(fsEntry.getYear())
				.oemId(fsEntry.getOemId())
				.requestedMonth(c.get(Calendar.MONTH) + 1)
				.requestedYear(c.get(Calendar.YEAR))
				.siteId(fsEntry.getSiteId())
				.version(fsEntry.getVersion())
				.fsByFiscalYear(false)
				.fsTime(tillEpoch)
				.oemConfig(oemConfig)
				.accountingInfo(accountingInfo)
				.tillEpoch(tillEpoch)
				.includeM13(includeM13)
				.addM13BalInDecBalances(addM13BalInDecBalances)
				.build();

		fetchTrialBalanceRowForGlAccounts(context, fsEntry);

		if(isRoundOffAndOffset(oemConfig, accountingInfo)){
			return getFsDetailsReportWithRoundOff(context);
		}

		return getFsCellCodeDetailsReport(context);
	}

	@Override
	public FsGroupCodeDetailsResponseDto computeFsGroupCodeDetails(String oemId, Integer oemFsYear, Integer oemFsVersion, long tillEpoch, boolean includeM13, boolean addM13BalInDecBalances, String siteId) {
		List<FSEntry> fsEntries = TCollectionUtils.nullSafeList(fsEntryRepo.findByOemYearVersionAndSite(oemId, oemFsYear, oemFsVersion,
				                   UserContextProvider.getCurrentDealerId(), UserContextUtils.getSiteIdFromUserContext()));

		if (TCollectionUtils.isEmpty(fsEntries)) {
			return new FsGroupCodeDetailsResponseDto();
		}

		List<FSEntry> fsEntriesOfTypeOem = new ArrayList<>();
		List<FSEntry> fsEntriesOfTypeInternal = new ArrayList<>();

		fsEntries.stream().forEach(fsEntry -> {
			if (FSType.INTERNAL.name().equals(fsEntry.getFsType())) {
				fsEntriesOfTypeInternal.add(fsEntry);
			} else if (FSType.OEM.name().equals(fsEntry.getFsType())) {
				fsEntriesOfTypeOem.add(fsEntry);
			}
		});

		FSEntry fsEntry;
		if (TCollectionUtils.isEmpty(fsEntriesOfTypeOem)) {
			sortFsEntriesByCreatedTime(fsEntriesOfTypeInternal);
			fsEntry = fsEntriesOfTypeInternal.get(FIRST_RECORD);
		} else {
			sortFsEntriesByCreatedTime(fsEntriesOfTypeOem);
			fsEntry = fsEntriesOfTypeOem.get(FIRST_RECORD);
		}

		FsGroupCodeDetailsResponseDto responseDto = new FsGroupCodeDetailsResponseDto();
		MonthInfo activeMonthInfo = getActiveMonthInfo();
		Calendar c = TimeUtils.buildCalendar(tillEpoch);
		if(isFutureMonth(activeMonthInfo, c.get(Calendar.YEAR), c.get(Calendar.MONTH) )){
			return FsGroupCodeDetailsResponseDto.builder().build();
		}
		log.info("includeM13: {}", includeM13);
		Map<String, TrialBalanceRow> trialBalanceRowMap = getGlAccountIdToTrialBMap(accountingService.getCYTrialBalanceTillDayOfMonth(tillEpoch,Sets.newConcurrentHashSet(),
				false,
				includeM13,
				DpUtils.doUseTbGeneratorV2VersionForFsInOem(), addM13BalInDecBalances));

		FsReportContext context = FsReportContext.builder()
				.fsId(fsEntry.getId())
				.fromMonth(Calendar.JANUARY+1)
				.fromYear(c.get(Calendar.YEAR))
				.oemFsYear(oemFsYear)
				.oemId(oemId)
				.requestedMonth(c.get(Calendar.MONTH) + 1)
				.requestedYear(c.get(Calendar.YEAR))
				.trialBalanceRowMap(trialBalanceRowMap)
				.version(oemFsVersion)
				.fsByFiscalYear(false)
				.siteId(siteId)
				.build();

		Map<String, GLAccount> idGlAccountMap = TCollectionUtils.transformToMap(accountingService.getGLAccounts(UserContextProvider.getCurrentDealerId()),GLAccount::getId);
		Map<String, FsGroupCodeDetail> groupCodeDisplayNameVsDetailsMap = Maps.newHashMap();
		Map<String, List<String>> groupCodeVsGlAccountsMap = getGlAccountsForFsCellGroups(context);
		Map<String,String> oemFsGroupCodeVsDisplayNameMap = TCollectionUtils.nullSafeList(oemFsCellGroupRepo.findNonDeletedByOemIdYearVersionAndCountry(oemId, oemFsYear, oemFsVersion, dealerConfig.getDealerCountryCode()))
				.stream().collect(Collectors.toMap(AccountingOemFsCellGroup::getGroupCode, AccountingOemFsCellGroup::getGroupDisplayName));
		for(Map.Entry<String,List<String>> entry : groupCodeVsGlAccountsMap.entrySet()){
			if(!oemFsGroupCodeVsDisplayNameMap.containsKey(entry.getKey())){
				log.warn("invalid mapping exists with deleted group code : {}",entry.getKey());
				continue;
			}
			BigDecimal mtdBalance = BigDecimal.ZERO;
			BigDecimal ytdBalance = BigDecimal.ZERO;
			long mtdCount = 0;
			long ytdCount = 0;
			List<String> dependentGlAccounts = new ArrayList<>();
			List<String> glAccounts = TCollectionUtils.nullSafeList(entry.getValue());
			for(String glAccountId : glAccounts){
				String glAccountNumber = glAccountId;
				GLAccount glAccount = idGlAccountMap.get(glAccountId);
				if(Objects.nonNull(glAccount) && !glAccount.getAccountNumber().equals(TConstants.BLANK_STRING)){
					glAccountNumber = glAccount.getAccountNumber();
				}
				if(trialBalanceRowMap.containsKey(glAccountId)){
					TrialBalanceRow data = trialBalanceRowMap.get(glAccountId);
					mtdBalance = mtdBalance.add(getMtdBalance(data));
					ytdBalance = ytdBalance.add(getYtdBalance(data));
					mtdCount = mtdCount + data.getCount();
					ytdCount = ytdCount + data.getYtdCount();
					dependentGlAccounts.add(glAccountNumber);
				}
			}
			FsGroupCodeDetail groupCodeDetail = FsGroupCodeDetail.builder().mtdBalance(mtdBalance)
					.ytdBalance(ytdBalance)
					.mtdCount(mtdCount)
					.ytdCount(ytdCount)
					.dependentGlAccounts(dependentGlAccounts)
					.build();
			String groupCodeDisplayName = oemFsGroupCodeVsDisplayNameMap.get(entry.getKey());
			groupCodeDisplayNameVsDetailsMap.put(groupCodeDisplayName, groupCodeDetail);
		}
		responseDto.setDetails(groupCodeDisplayNameVsDetailsMap);
		responseDto.setDate(TimeUtils.getStringFromEpoch(tillEpoch,"dd-MM-yyyy"));
		return responseDto;
	}

	private void sortFsEntriesByCreatedTime(List<FSEntry> fsEntries) {
		Collections.sort(fsEntries, new Comparator<FSEntry>() {
			@Override
			public int compare(FSEntry fsEntry1, FSEntry fsEntry2) {
				return Long.compare(fsEntry1.getCreatedTime(), fsEntry2.getCreatedTime());
			}
		});
	}

	@Override
	public FsCellCodeDetailsResponseDto computeFsDetails(String oemId, Integer oemFsYear, Integer oemFsVersion, long tillEpoch, boolean includeM13, boolean addM13BalInDecBalances){
		FSEntry fsEntry = fsEntryRepo.findDefaultType(oemId, oemFsYear ,UserContextProvider.getCurrentDealerId(), UserContextUtils.getSiteIdFromUserContext());
		MonthInfo activeMonthInfo = getActiveMonthInfo();
		Calendar c = TimeUtils.buildCalendar(tillEpoch);
		if(isFutureMonth(activeMonthInfo, c.get(Calendar.YEAR), c.get(Calendar.MONTH) )){
			return FsCellCodeDetailsResponseDto.builder().build();
		}
		Map<String, TrialBalanceRow> trialBalanceRowMap = getGlAccountIdToTrialBMap(accountingService.getCYTrialBalanceTillDayOfMonth(tillEpoch,
				Sets.newConcurrentHashSet(),
				false,
				includeM13,
				DpUtils.doUseTbGeneratorV2VersionForFsInOem(), addM13BalInDecBalances));

		FsReportContext context = FsReportContext.builder()
				.fsId(fsEntry.getId())
				.fromMonth(Calendar.JANUARY+1)
				.fromYear(c.get(Calendar.YEAR))
				.oemFsYear(oemFsYear)
				.oemId(oemId)
				.requestedMonth(c.get(Calendar.MONTH) + 1)
				.requestedYear(c.get(Calendar.YEAR))
				.trialBalanceRowMap(trialBalanceRowMap)
				.version(oemFsVersion)
				.fsByFiscalYear(false)
				.build();

		return getFsDetailsReportWithRoundOff(context);
	}

	@Override
	public FsCellCodeDetailsResponseDto computeFsCellCodeDetailsForFS(String fsId, long tillEpoch, boolean includeM13) {

		FSEntry fsEntry = fsEntryRepo.findByIdAndDealerIdWithNullCheck(fsId, UserContextProvider.getCurrentDealerId());

		MonthInfo activeMonthInfo = getActiveMonthInfo();
		Calendar c = TimeUtils.buildCalendar(tillEpoch);


		if(isFutureMonth(activeMonthInfo, c.get(Calendar.YEAR), c.get(Calendar.MONTH) )){
			return FsCellCodeDetailsResponseDto.builder().build();
		}

		int fiscalStartMonth = getFiscalYearStartMonth();

		Map<String, TrialBalanceRow> trialBalanceRowMap =
				getCumulativeTrialBalance(fiscalStartMonth, c);

		YearMonth yearMonth = getFYStartDetails(c.get(Calendar.YEAR), c.get(Calendar.MONTH), fiscalStartMonth);

		FsReportContext context = FsReportContext.builder()
				.fsId(fsId)
				.fromYear(yearMonth.getYear())
				.fromMonth(yearMonth.getMonthValue())
				.oemFsYear(fsEntry.getYear())
				.oemId(fsEntry.getOemId())
				.requestedMonth(c.get(Calendar.MONTH) + 1)
				.requestedYear( c.get(Calendar.YEAR))
				.siteId(fsEntry.getSiteId())
				.trialBalanceRowMap(trialBalanceRowMap)
				.version(fsEntry.getVersion())
				.fsByFiscalYear(true)
				.fsTime(tillEpoch)
				.build();

		return getFsCellCodeDetailsReport(context);
	}

	@Override
	public FSEntry createFsmInfoIfPresent(OEM oem, int year, int version){
		FSEntry previousInfo = fsEntryRepo.findByOem(oem.name(), version, UserContextProvider.getCurrentDealerId());
		if(Objects.isNull(previousInfo)){
			log.info("not creating oemMappingInfo for {} {}", oem.name(), year);
			return null;
		}
		return fsEntryService.createFSEntry(FsEntryCreateDto.builder().oemId(oem).year(year).version(version).build());
	}

	private Map<String, TrialBalanceRow> getCumulativeTrialBalance(int fiscalStartMonth, Calendar tillDate) {

		int requestedMoth = tillDate.get(Calendar.MONTH);
		int fiscalStartYear = tillDate.get(Calendar.YEAR);

		if(requestedMoth < fiscalStartMonth){
			fiscalStartYear = fiscalStartYear-1;
		}

		TrialBalance trialBalance = accountingService.getFSTrialBalanceTillDayOfMonth(fiscalStartYear,
						fiscalStartMonth,tillDate.get(Calendar.YEAR), tillDate.get(Calendar.MONTH)
						, tillDate.getTimeInMillis());

		return trialBalance.getAccountRows().stream().collect(Collectors.toMap(TrialBalanceRow::getAccountId, k -> k));
	}

	@Override
	public List<OEMFsCellCodeSnapshotResponseDto> getFsCellCodeDetails(String oemId, Integer oemFsYear, Integer oemFsVersion,
																	   long tillEpoch, Set<String> codes, boolean includeM13, boolean addM13BalInDecBalances) {
		FSEntry fsEntry = fsEntryRepo.findDefaultType(oemId,oemFsYear, UserContextProvider.getCurrentDealerId(), UserContextUtils.getSiteIdFromUserContext());
		List<OEMFsCellCodeSnapshotResponseDto> oemFsCellCodeSnapshotResponseDtoList = Lists.newArrayList();
		FsCellCodeDetailsResponseDto fsCellCodeDetailsResponseDto = computeFsCellCodeDetails(fsEntry, tillEpoch, includeM13, addM13BalInDecBalances);
		Map<String, FsCodeDetail> codeVsDetailsMap = fsCellCodeDetailsResponseDto.getCodeVsDetailsMap();
		codeVsDetailsMap.keySet().forEach(key -> {
			if(codes.contains(key)) {
				OEMFsCellCodeSnapshotResponseDto dto = new OEMFsCellCodeSnapshotResponseDto(
						key, codeVsDetailsMap.get(key).getValue());
				oemFsCellCodeSnapshotResponseDtoList.add(dto);
			}
		});

		return oemFsCellCodeSnapshotResponseDtoList;
	}

	// Uses month indexing from {1,......, 12}
	private FsCellCodeDetailsResponseDto getFsCellCodeDetailsReport(FsReportContext fsrContext) {

		//log.info("IN getFsCellCodeDetailsReport time {}, FsReportContext {}", System.currentTimeMillis(), fsrContext);

		List<AccountingOemFsCellCode> fsCellCodes =
				getOemTMappingListFromCache(fsrContext.getOemId(), fsrContext.getOemFsYear(), fsrContext.getVersion(), dealerConfig.getDealerCountryCode());

		Map<String, AccountingOemFsCellCode> codeVsCellInfoMap = TCollectionUtils.transformToMap(fsCellCodes, AccountingOemFsCellCode::getCode);

		List<AccountingOemFsCellCode> derivedFSCellCode = Lists.newArrayList();
		List<AccountingOemFsCellCode> nonDerivedFSCellCode = Lists.newArrayList();
		List<AccountingOemFsCellCode> monthlyFSCellCodes = Lists.newArrayList();
		Map<String, List<String>> groupCodeVsGlAccountsMap = getGlAccountsForFsCellGroups(fsrContext);
		Map<String, MemoWorksheet> memoKeyToWorksheetMap = getMemoWorksheetMap(fsrContext);
		Map<String, HCWorksheet> hcKeyToWorksheetmap = getHCWorksheetMap(fsrContext);
		OemConfig oemConfig = getOemConfig(fsrContext.getOemId());
		Set<String> monthlyCellCodes = new HashSet<>();

		OemFsCellContext oemFsCellContext = OemFsCellContext.builder()
				.requestedYear(fsrContext.getRequestedYear())
				.requestedMonth(fsrContext.getRequestedMonth())
				.codeVsCellInfoMap(codeVsCellInfoMap)
				.groupCodeVsGlAccountsMap(groupCodeVsGlAccountsMap)
				.trialBalanceRowMap(fsrContext.getTrialBalanceRowMap())
				.memoKeyToValueMap(memoKeyToWorksheetMap)
				.hcKeyValueMap(hcKeyToWorksheetmap)
				.roundOff(getRoundOffProperty(oemConfig))
				.defaultPrecision(getDefaultPrecision(fsrContext.getOemId(), oemConfig))
				.monthlyCodes(monthlyCellCodes)
				.oemConfig(oemConfigRepo.findByOemId(fsrContext.getOemId(), dealerConfig.getDealerCountryCode()))
				.accountingInfo(aiService.find(UserContextProvider.getCurrentDealerId()))
				.fsTime(fsrContext.getFsTime())
				.build();


		fsCellCodes.forEach(c -> {
			if(c.isDerived()){
				derivedFSCellCode.add(c);
				oemFsCellContext.getCodeVsExpressionMap().put(c.getCode(), "(" + c.getExpression() + ")");
				if(OemCellSubType.MONTHLY.name().equalsIgnoreCase(c.getSubType())){
					if(Integer.parseInt(c.getAdditionalInfo().get(additionInfoField_month)) != oemFsCellContext.getRequestedMonth()){
						monthlyCellCodes.add(c.getCode());
						monthlyFSCellCodes.add(c);
					}
				}
			}else{
				nonDerivedFSCellCode.add(c);
			}
		});

		// Start : non-derived codes
		computeNonDerivedCellCodes(oemFsCellContext, nonDerivedFSCellCode, groupCodeVsGlAccountsMap);
		// End : non-derived codes
		// Start : monthly code's values

		if(FinancialStatementUtils.useSnapshotValuesInFS(oemFsCellContext)){
			fetchSnapshotValues(fsrContext, oemFsCellContext);
		}

		monthlyFSCellCodes.forEach(c ->{
			if(Integer.parseInt(c.getAdditionalInfo().get(additionInfoField_month)) == fsrContext.getFromMonth()){
				oemFsCellContext.setFsCellCode(c);
				computeMonthlyExpressions(oemFsCellContext);
			}
		});
		Set<String> codesRelatedToMonthlyPL = new HashSet<>();
		Set<String> glIdsRelatedToMonthlyPL = new HashSet<>();

		monthlyFSCellCodes.forEach(c -> {
			String codeIdentifier = c.getAdditionalInfo().get(additionInfoField_codeIdentifier);
			if(Integer.parseInt(c.getAdditionalInfo().get(additionInfoField_month)) == fsrContext.getFromMonth()){
				String expression = oemFsCellContext.getCodeIdentifierVsExpressionMap().get(codeIdentifier);
				codesRelatedToMonthlyPL.addAll(OemFSUtils.getCodesFromExpression(expression));
			}
		});

		codesRelatedToMonthlyPL.forEach(c -> {
			try{
				glIdsRelatedToMonthlyPL.addAll(
						TCollectionUtils.nullSafeEmptyList(
								oemFsCellContext.getGroupCodeVsGlAccountsMap()
										.get(oemFsCellContext.getCodeVsCellInfoMap().get(c).getGroupCode())));
			}catch (Exception e){
				log.error(String.format("something wrong with this cell code %s", c));
				throw new RuntimeException(e);
			}
		});

		fsrContext.setGlIdsRelatedToMonthlyPL(glIdsRelatedToMonthlyPL);
		Map<Integer, Map<String, Map<String, BigDecimal>>> glBalCntInfoForFS = getGlBalCntInfoForFS(fsrContext.toAccountingFSRContext());

		Set<Integer> validMonths = TimeUtils.getMonthsBetween(fsrContext.getFromYear(),
				fsrContext.getFromMonth()-1, fsrContext.getRequestedYear(), fsrContext.getRequestedMonth()-1);


		for (AccountingOemFsCellCode monthlyFsCellCode : monthlyFSCellCodes) {

			int cellMonth = Integer.parseInt(monthlyFsCellCode.getAdditionalInfo().get(additionInfoField_month));

			if(validMonths.contains(cellMonth-1)) {
				computeMonthlyFsCellCodeValue(oemFsCellContext, glBalCntInfoForFS, monthlyFsCellCode);
			}else{
				oemFsCellContext.getCodeVsDetailsMap().put(monthlyFsCellCode.getCode(),
						FsCodeDetail.builder()
								.value(BigDecimal.ZERO)
								.derived(true)
								.dependentFsCellCodes(monthlyFsCellCode.getDependentFsCellCodes())
								.build());
				oemFsCellContext.getCodeVsValueMap().put(monthlyFsCellCode.getCode(), BigDecimal.ZERO);
			}
		}
		// End : monthly code's values
		// Start : calculating values for derived code
		derivedFSCellCode.forEach(c -> {
			if (oemFsCellContext.getCodeVsDetailsMap().containsKey(c.getCode())) {
				return;
			}
			oemFsCellContext.setFsCellCode(c);
			computeDerivedCellValue(oemFsCellContext);
		});

		// End : calculating values for derived code
		return FsCellCodeDetailsResponseDto.builder().accountingOemFsCellCodes(fsCellCodes)
				.codeVsDetailsMap(oemFsCellContext.getCodeVsDetailsMap())
				.build();
	}

	private void computeNonDerivedCellCodes(OemFsCellContext oemFsCellContext, List<AccountingOemFsCellCode> nonDerivedFSCellCodes, Map<String, List<String>> groupCodeVsGlAccountsMap){
		nonDerivedFSCellCodes.forEach(c -> {
			oemFsCellContext.setFsCellCode(c);
			String code = c.getCode();
			String groupCode = c.getGroupCode();
			FsCellCodeSource fsCellCodeSource = c.getSource() == null?FsCellCodeSource.TRIAL_BALANCE:c.getSource();
			if(oemFsCellContext.getCodeVsDetailsMap().containsKey(code)){
				return;
			}
			FsCodeDetail fsCodeDetail = null;
			switch (fsCellCodeSource){
				case MEMO_WRKSHT:
					fsCodeDetail = computeNonDerivedForMemo(oemFsCellContext);
					break;
				case HEADCOUNT_WRKSHT:
					// TODO
					fsCodeDetail = computeNonDerivedForHC(oemFsCellContext);
					break;
				case DEALER_CONFIG:
					return;
				case DATE_MONTH:
					fsCodeDetail = getCodeDetailsForDateMonth(oemFsCellContext);
					break;

				case CUSTOM_SOURCE:
					fsCodeDetail = getDetailsForCustomSource(oemFsCellContext, c);
					break;

				default://TRIAL_BALANCE
					if(!groupCodeVsGlAccountsMap.containsKey(groupCode)){
						fsCodeDetail = FsCodeDetail.builder().value(getValueWithPrecision(BigDecimal.ZERO, oemFsCellContext)).build();
					}else{
						fsCodeDetail= computeNonDerivedCellValue(oemFsCellContext);
					}
			}

			if(fsCodeDetail == null){
				log.error("Looks like an invalid config {} {} {}",code,groupCode,c);
				return; // how did we end here.
			}
			oemFsCellContext.getCodeVsDetailsMap().put(code, fsCodeDetail);
			oemFsCellContext.getCodeVsValueMap().put(code, fsCodeDetail.getValue());
		});
	}

	private void fetchSnapshotValues(FsReportContext fsrContext, OemFsCellContext oemFsCellContext ){
		Set<String> monthlyCellCodes = oemFsCellContext.getMonthlyCodes();
		List<OEMFsCellCodeSnapshot> snapshots = oemFsCellCodeSnapshotRepo.findSnapshotByCodes(
				fsrContext.getFsId(), monthlyCellCodes, UserContextProvider.getCurrentDealerId());
		log.info("fId {} monthly cell codes {} dealerId {} snapshots {}", fsrContext.getFsId(), monthlyCellCodes.size(), UserContextProvider.getCurrentDealerId(), (long) snapshots.size());
		log.info("monthly cell codes {}", JsonUtil.toJson(monthlyCellCodes));

		Map<Integer, Map<String, OEMFsCellCodeSnapshot>> monthToCellCodeSnapshots = snapshots.stream().collect(
				groupingBy(OEMFsCellCodeSnapshot::getMonth, Collectors.toMap(OEMFsCellCodeSnapshot::getCode, x -> x, (oldValue, newValue) -> newValue)));
		oemFsCellContext.setMonthToCellCodeSnapshots(monthToCellCodeSnapshots);
		log.info("cell code snapshots months {}", JsonUtil.toJson(monthToCellCodeSnapshots.keySet()));
	}

	private FsCellCodeDetailsResponseDto getFsDetailsReportWithRoundOff(FsReportContext fsrContext) {

		//log.info("IN getFsCellCodeDetailsReport time {}, FsReportContext {}", System.currentTimeMillis(), fsrContext);
		log.info("calculating FS Values with roundOff offsetting!");

		List<AccountingOemFsCellCode> fsCellCodes =
				getOemTMappingListFromCache(fsrContext.getOemId(), fsrContext.getOemFsYear(), fsrContext.getVersion(), dealerConfig.getDealerCountryCode());

		Map<String, AccountingOemFsCellCode> codeVsCellInfoMap = TCollectionUtils.transformToMap(fsCellCodes, AccountingOemFsCellCode::getCode);

		List<AccountingOemFsCellCode> derivedFSCellCodes = Lists.newArrayList();
		List<AccountingOemFsCellCode> nonDerivedFSCellCodes = Lists.newArrayList();
		List<AccountingOemFsCellCode> monthlyFSCellCodes = Lists.newArrayList();
		Map<String, List<String>> groupCodeVsGlAccountsMap = getGlAccountsForFsCellGroups(fsrContext);
		Map<String, MemoWorksheet> memoKeyToWorksheetMap = getMemoWorksheetMap(fsrContext);
		Map<String, HCWorksheet> hcKeyToWorksheetmap = getHCWorksheetMap(fsrContext);
		OemConfig oemConfig = getOemConfig(fsrContext.getOemId());
		List<AccountingOemFsCellCode> roundOffCells = new ArrayList<>();
		Set<String> monthlyCodes = new HashSet<>();

		OemFsCellContext oemFsCellContext = OemFsCellContext.builder()
				.requestedYear(fsrContext.getRequestedYear())
				.requestedMonth(fsrContext.getRequestedMonth())
				.codeVsCellInfoMap(codeVsCellInfoMap)
				.groupCodeVsGlAccountsMap(groupCodeVsGlAccountsMap)
				.trialBalanceRowMap(fsrContext.getTrialBalanceRowMap())
				.memoKeyToValueMap(memoKeyToWorksheetMap)
				.hcKeyValueMap(hcKeyToWorksheetmap)
				.roundOff(getRoundOffProperty(oemConfig))
				.defaultPrecision(getDefaultPrecision(fsrContext.getOemId(), oemConfig))
				.derivedFSCellCodes(derivedFSCellCodes)
				.nonDerivedFSCellCodes(nonDerivedFSCellCodes)
				.monthlyFSCellCodes(monthlyFSCellCodes)
				.fsCellCodes(fsCellCodes)
				.monthlyCodes(monthlyCodes)
				.oemConfig(oemConfigRepo.findByOemId(fsrContext.getOemId(), dealerConfig.getDealerCountryCode()))
				.accountingInfo(aiService.find(UserContextProvider.getCurrentDealerId()))
				.fsTime(fsrContext.getFsTime())
				.build();

		fsCellCodes.forEach(c -> {
			if (c.isDerived()) {
				derivedFSCellCodes.add(c);
				oemFsCellContext.getCodeVsExpressionMap().put(c.getCode(), "(" + c.getExpression() + ")");
				if (OemCellSubType.MONTHLY.name().equalsIgnoreCase(c.getSubType())) {
					monthlyCodes.add(c.getCode());
					if (Integer.parseInt(c.getAdditionalInfo().get(additionInfoField_month)) != oemFsCellContext.getRequestedMonth()) {
						monthlyFSCellCodes.add(c);
					}
				}
			}else{
				if(FsCellCodeSource.ROUNDOFF_OFFSET.equals(c.getSource())){
					roundOffCells.add(c);
				}else{
					nonDerivedFSCellCodes.add(c);
				}

			}
		});

		FsCellCodeDetailsResponseDto roundOffDto = calculateCellCodeDetails(oemFsCellContext, fsrContext);
		mergeFSDetails(roundOffDto, roundOffDto, ROUNDED_VALUE);

		if(TCollectionUtils.isEmpty(roundOffCells)){
			log.warn("RoundOff offset config in cell codes is missing for {} {}", fsrContext.getOemId(), fsrContext.getFromYear());
			return roundOffDto;
		}

		AccountingOemFsCellCode cell = roundOffCells.get(0);
		String minuendCellCode = cell.getAdditionalInfo().get(OemConfig.FIRST_CODE);
		String subtrahendCellCode = cell.getAdditionalInfo().get(OemConfig.SECOND_CODE);
		String memoKey = cell.getAdditionalInfo().get(OemConfig.MEMO_KEY);
		if(Objects.nonNull(oemFsCellContext.getAccountingInfo())
				&& TCollectionUtils.isNotEmpty(oemFsCellContext.getAccountingInfo().getOemToOffsetCellMap())
				&& Objects.nonNull(oemFsCellContext.getAccountingInfo().getOemToOffsetCellMap().get(fsrContext.getOemId()))){

			memoKey = oemFsCellContext.getAccountingInfo().getOemToOffsetCellMap().get(fsrContext.getOemId());
			log.info("memo key {} to offset using from accountingInfo", memoKey);
		}

		if (Objects.isNull(memoKey) || Objects.isNull(minuendCellCode) || Objects.isNull(subtrahendCellCode)) {
			log.info("minuendCellCode {} subtrahendCellCode {} memoKey {}", minuendCellCode, subtrahendCellCode, memoKey);
			log.error("some data is missing in OemConfig for offset roundOff for oem {}", oemConfig.getOemId());
			return roundOffDto;
		}

		MemoWorksheet mWorksheet = memoKeyToWorksheetMap.get(memoKey);

		if(Objects.isNull(mWorksheet) || TCollectionUtils.isEmpty(mWorksheet.getValues())){
			log.error("memo migration is missing for key {} for {} {}", memoKey, fsrContext.getOemId(), fsrContext.getFromYear());
			return roundOffDto;
		}

		// if we round off at GlAccount level, offsetting value should be diff between liabilities and assets
		if(FinancialStatementUtils.isRoundOffGLBalances(fsrContext)){
			return computeFsDetailsWithGLRoundOff(fsrContext, mWorksheet, oemFsCellContext, roundOffDto, minuendCellCode, subtrahendCellCode);
		}

		FsCellCodeDetailsResponseDto nonRoundOffDto = roundOffDto;

		if (oemFsCellContext.isRoundOff()) {
			log.info("calculating cellCode details without roundOff");
			oemFsCellContext.setRoundOff(false);
			nonRoundOffDto = calculateCellCodeDetails(oemFsCellContext, fsrContext);
		}

		mergeFSDetails(nonRoundOffDto, roundOffDto, ORIGINAL_VALUE);

		try {
			BigDecimal firstDiff = roundOffDto.getCodeVsDetailsMap().get(minuendCellCode).getValue().subtract(
					roundOffDto.getCodeVsDetailsMap().get(minuendCellCode).getOriginalValue());
			BigDecimal secondDiff = roundOffDto.getCodeVsDetailsMap().get(subtrahendCellCode).getValue().subtract(
					roundOffDto.getCodeVsDetailsMap().get(subtrahendCellCode).getOriginalValue());

			BigDecimal diff = firstDiff.subtract(secondDiff);
			BigDecimal diff1 = firstDiff.setScale(0, BigDecimal.ROUND_HALF_UP)
					.subtract(secondDiff.setScale(0, BigDecimal.ROUND_HALF_UP));
			log.info("firstDiff {}, secondDiff {}, {} ~ {}, roundedDiffsDiff {}", firstDiff, secondDiff, diff,
					diff1.setScale(0, BigDecimal.ROUND_HALF_UP), diff1);

			if (diff1.compareTo(BigDecimal.ZERO) == 0) {
				mergeFSDetails(roundOffDto, roundOffDto, OFFSET_VALUE);
				return roundOffDto;
			}

			computeFsDetailsWithOffset(fsrContext, mWorksheet, diff1, oemFsCellContext, roundOffDto);

		} catch (Exception e) {
			log.error("Error while Offsetting RoundOff ", e);
		}

		return roundOffDto;
	}

	FsCellCodeDetailsResponseDto computeFsDetailsWithGLRoundOff(FsReportContext fsrContext, MemoWorksheet mWorksheet,
																OemFsCellContext oemFsCellContext, FsCellCodeDetailsResponseDto roundOffDto,
																String minuendCellCode, String subtrahendCellCode){
		try {
			BigDecimal firstValue = roundOffDto.getCodeVsDetailsMap().get(minuendCellCode).getValue();
			BigDecimal secondValue = roundOffDto.getCodeVsDetailsMap().get(subtrahendCellCode).getValue();
			BigDecimal diff = firstValue.subtract(secondValue);

			log.info("firstValue {}, secondValue {}, diff: {}", firstValue, secondValue, diff);

			if (diff.compareTo(BigDecimal.ZERO) == 0) {
				mergeFSDetails(roundOffDto, roundOffDto, OFFSET_VALUE);
				return roundOffDto;
			}

			computeFsDetailsWithOffset(fsrContext, mWorksheet, diff, oemFsCellContext, roundOffDto);

		} catch (Exception e) {
			log.error("Error while Offsetting RoundOff1 ", e);
		}

		return roundOffDto;
	}

	void computeFsDetailsWithOffset(FsReportContext fsrContext, MemoWorksheet mWorksheet, BigDecimal diff1,
									OemFsCellContext oemFsCellContext, FsCellCodeDetailsResponseDto roundOffDto){

		MemoValue mValue = mWorksheet.getValues().get(fsrContext.getRequestedMonth() - 1);
		mValue.setMtdValue(mValue.getMtdValue().add(diff1));
		mValue.setYtdValue(mValue.getYtdValue().add(diff1));

		oemFsCellContext.setRoundOff(true);
		FsCellCodeDetailsResponseDto responseDto = calculateCellCodeDetails(oemFsCellContext, fsrContext);
		mergeFSDetails(responseDto, roundOffDto, OFFSET_VALUE);
	}

	boolean isRoundOffAndOffset(OemConfig oemConfig, AccountingInfo accountingInfo){
		if(getRoundOffProperty(oemConfig)){
			if((Objects.nonNull(accountingInfo) && Objects.nonNull(accountingInfo.getFsRoundOffOffset()))){
				return accountingInfo.getFsRoundOffOffset();
			}
			return oemConfig.isEnableRoundOffOffset();
		}
		return false;
	}

	FsCellCodeDetailsResponseDto calculateCellCodeDetails(OemFsCellContext oemFsCellContext, FsReportContext fsrContext){
		removePreviouslyCalculatedValues(oemFsCellContext);
		List<AccountingOemFsCellCode> fsCellCodes = oemFsCellContext.getFsCellCodes();
		List<AccountingOemFsCellCode> derivedFSCellCode = oemFsCellContext.getDerivedFSCellCodes();
		List<AccountingOemFsCellCode> monthlyFSCellCodes = oemFsCellContext.getMonthlyFSCellCodes();
		List<AccountingOemFsCellCode> nonDerivedFSCellCodes = oemFsCellContext.getNonDerivedFSCellCodes();
		Map<String, List<String>> groupCodeVsGlAccountsMap = oemFsCellContext.getGroupCodeVsGlAccountsMap();
		Map<Integer, Map<String, Map<String, BigDecimal>>> glBalCntInfoForFS = oemFsCellContext.getGlBalCntInfoForFS();

		// Start : non-derived codes
		computeNonDerivedCellCodes(oemFsCellContext, nonDerivedFSCellCodes ,groupCodeVsGlAccountsMap);
		// End : non-derived codes
		// Start : monthly code's values

		monthlyFSCellCodes.forEach(c ->{
			if(Integer.parseInt(c.getAdditionalInfo().get(additionInfoField_month)) == fsrContext.getFromMonth()){
				oemFsCellContext.setFsCellCode(c);
				computeMonthlyExpressions(oemFsCellContext);
			}
		});

		if(FinancialStatementUtils.useSnapshotValuesInFS(oemFsCellContext)){
			fetchSnapshotValues(fsrContext, oemFsCellContext);
		}

		Set<String> codesRelatedToMonthlyPL = new HashSet<>();
		Set<String> glIdsRelatedToMonthlyPL = new HashSet<>();

		monthlyFSCellCodes.forEach(c -> {
			String codeIdentifier = c.getAdditionalInfo().get(additionInfoField_codeIdentifier);

			if(Integer.parseInt(c.getAdditionalInfo().get(additionInfoField_month)) == fsrContext.getFromMonth()){
				String expression = oemFsCellContext.getCodeIdentifierVsExpressionMap().get(codeIdentifier);
				codesRelatedToMonthlyPL.addAll(OemFSUtils.getCodesFromExpression(expression));
			}
		});

		codesRelatedToMonthlyPL.forEach(c -> {
			try{
				glIdsRelatedToMonthlyPL.addAll(
						TCollectionUtils.nullSafeEmptyList(
								oemFsCellContext.getGroupCodeVsGlAccountsMap()
										.get(oemFsCellContext.getCodeVsCellInfoMap().get(c).getGroupCode())));
			}catch (Exception e){
				log.error(String.format("something wrong with this cell code %s", c));
				throw new RuntimeException(e);
			}
		});

		if(TCollectionUtils.isEmpty(glBalCntInfoForFS)){
			fsrContext.setGlIdsRelatedToMonthlyPL(glIdsRelatedToMonthlyPL);
			glBalCntInfoForFS = getGlBalCntInfoForFS(fsrContext.toAccountingFSRContext());
			oemFsCellContext.setGlBalCntInfoForFS(glBalCntInfoForFS);
		}

		Set<Integer> validMonths = TimeUtils.getMonthsBetween(fsrContext.getFromYear(),
				fsrContext.getFromMonth()-1, fsrContext.getRequestedYear(), fsrContext.getRequestedMonth()-1);

		for (AccountingOemFsCellCode monthlyFsCellCode : monthlyFSCellCodes) {

			int cellMonth = Integer.parseInt(monthlyFsCellCode.getAdditionalInfo().get(additionInfoField_month));

			if(validMonths.contains(cellMonth-1)) {
				computeMonthlyFsCellCodeValue(oemFsCellContext, glBalCntInfoForFS, monthlyFsCellCode);

			}else{
				oemFsCellContext.getCodeVsDetailsMap().put(monthlyFsCellCode.getCode(),
						FsCodeDetail.builder()
								.value(BigDecimal.ZERO)
								.derived(true)
								.dependentFsCellCodes(monthlyFsCellCode.getDependentFsCellCodes())
								.build());
				oemFsCellContext.getCodeVsValueMap().put(monthlyFsCellCode.getCode(), BigDecimal.ZERO);
			}
		}
		// End : monthly code's values
		// Start : calculating values for derived code
		derivedFSCellCode.forEach(c -> {
			if (oemFsCellContext.getCodeVsDetailsMap().containsKey(c.getCode())) {
				return;
			}
			oemFsCellContext.setFsCellCode(c);
			computeDerivedCellValue(oemFsCellContext);
		});

		return  FsCellCodeDetailsResponseDto.builder().accountingOemFsCellCodes(fsCellCodes)
				.codeVsDetailsMap(oemFsCellContext.getCodeVsDetailsMap())
				.build();
	}

	private void compare(FsCellCodeDetailsResponseDto roundOffDto, FsCellCodeDetailsResponseDto oldResponseDto) {
		for(String s: oldResponseDto.getCodeVsDetailsMap().keySet()){
			if(roundOffDto.getCodeVsDetailsMap().get(s) == null){
				log.error("FS CellCode value missing {}", s);
			}else if(roundOffDto.getCodeVsDetailsMap().get(s).getValue().compareTo(oldResponseDto.getCodeVsDetailsMap().get(s).getValue()) != 0){
				log.error("FS values not matching for cellCode: {}, {} <> {}", s, roundOffDto.getCodeVsDetailsMap().get(s).getValue()
						, oldResponseDto.getCodeVsDetailsMap().get(s).getValue());
			}
		}
	}

	private void removePreviouslyCalculatedValues(OemFsCellContext oemFsCellContext) {
		oemFsCellContext.setCodeVsDetailsMap(new HashMap<>());
		oemFsCellContext.setCodeVsValueMap(new HashMap<>());
	}

	void mergeFSDetails(FsCellCodeDetailsResponseDto nonRoundOffDto, FsCellCodeDetailsResponseDto roundOffDto, String type){
		Map<String, FsCodeDetail> roundOffValuesMap = roundOffDto.getCodeVsDetailsMap();
		Map<String, FsCodeDetail> otherValuesMap = nonRoundOffDto.getCodeVsDetailsMap();
		for(String cellCode: roundOffValuesMap.keySet()){
			switch (type){
				case OFFSET_VALUE:
					roundOffValuesMap.get(cellCode).setValueAfterOffset(otherValuesMap.get(cellCode).getValue());
					roundOffValuesMap.get(cellCode).setValue(otherValuesMap.get(cellCode).getValue());
					break;
				case ORIGINAL_VALUE:
					roundOffValuesMap.get(cellCode).setOriginalValue(otherValuesMap.get(cellCode).getValue());
					break;
				case ROUNDED_VALUE:
					roundOffValuesMap.get(cellCode).setRoundedValue(otherValuesMap.get(cellCode).getValue());
					break;
			}
		}
	}

	private FsCodeDetail getCodeDetailsForDateMonth(OemFsCellContext oemFsCellContext) {
		return FsCodeDetail.builder().value(new BigDecimal(oemFsCellContext.getRequestedMonth())).build();
	}

	private FsCodeDetail getDetailsForCustomSource(OemFsCellContext oemFsCellContext, AccountingOemFsCellCode cell) {
		String customSourceType = cell.getAdditionalInfo().get(SOURCE_TYPE);
		String value = null;
		switch (customSourceType){
			case "CITY_STATE":
				value = dealerConfig.getDealerCityState();
				break;
			case "ADDRESS":
				value = dealerConfig.getDealerPostBox();
				break;
			case "DEALER_NAME":
				value = dealerConfig.getDealerName();
				break;
			case "FROM_DATE":
				value = TimeUtils.getStringFromEpoch(TimeUtils.getYearStart(oemFsCellContext.getFsTime()), NCT_DATE_FORMAT);;
				break;
			case "TO_DATE":
				value = TimeUtils.getStringFromEpoch(oemFsCellContext.getFsTime(), NCT_DATE_FORMAT);
				break;
			default:
				log.warn("unhandled CUSTOM_SOURCE type in FS cell {}", customSourceType);

		}
		return FsCodeDetail.builder().stringValue(value).build();
	}

	private Map<String, HCWorksheet> getHCWorksheetMap(FsReportContext fsrContext) {
		List<HCWorksheet> hcWorksheets = hcWorksheetRepo.findByFsId(fsrContext.getFsId());
		return TCollectionUtils.transformToMap(TCollectionUtils.nullSafeCollection(hcWorksheets), (hc)->{
			return hc.getDepartment()+"_"+hc.getPosition();
		});

	}

	private Map<String, MemoWorksheet> getMemoWorksheetMap(FsReportContext fsrContext) {
		if(!fsrContext.isFsByFiscalYear()){
			return TCollectionUtils.transformToMap(TCollectionUtils.nullSafeCollection(
					memoWorksheetRepo.findForOemByYearOptimized(fsrContext.getFsId(), getCurrentDealerId())), MemoWorksheet::getKey);
		}

		Set<Integer> years = new HashSet<>();
		years.add(fsrContext.getFromYear());
		years.add(fsrContext.getRequestedYear());

		List<MemoWorksheet> fullList = memoWorksheetRepo.findForOemByYearsOptimized(fsrContext.getFsId(), getCurrentDealerId());

		//log.info(" <----> fullMemoList {}", JsonUtil.toJson(fullList));

		List<MemoWorksheet> reqYearList = fullList.stream().filter(e -> e.getYear() == fsrContext.getRequestedYear()).collect(Collectors.toList());

		Map<String, List<MemoWorksheet>> map = new HashMap<>();

		fullList.forEach(memo -> map.computeIfAbsent(memo.getKey(), k -> new ArrayList<>()).add(memo));

		for(List<MemoWorksheet> sheets: map.values()){
			updateMemoWorksheetsByFY(sheets, fsrContext);
		}

		return TCollectionUtils.transformToMap(TCollectionUtils.nullSafeCollection(reqYearList), MemoWorksheet::getKey);
	}

	private List<String> getGlAccountsForFsCellCode(String cellCode, OemFsCellContext oemFsCellContext) {
		List<String> glAccounts = Lists.newArrayList();
		glAccounts.addAll(
				TCollectionUtils.nullSafeEmptyList(
						oemFsCellContext.getGroupCodeVsGlAccountsMap()
								.get(oemFsCellContext.getCodeVsCellInfoMap().get(cellCode).getGroupCode())));
		return glAccounts;
	}

	private void computeMonthlyFsCellCodeValue(OemFsCellContext context, Map<Integer ,Map<String, Map<String, BigDecimal>>> glBalCntInfoForFS, AccountingOemFsCellCode monthlyFsCellCode) {

		BigDecimal cellValue = BigDecimal.ZERO;
		Map<String, BigDecimal> cellCodeToValueMap = Maps.newHashMap();
		String codeIdentifier = monthlyFsCellCode.getAdditionalInfo().get(additionInfoField_codeIdentifier);

		for (String dependentFsCellCode : OemFSUtils.getCodesFromExpression(context.getCodeIdentifierVsExpressionMap().get(codeIdentifier))) {
			BigDecimal dependentCellValue = BigDecimal.ZERO;
			dependentCellValue = getDependentCellValue(context, glBalCntInfoForFS, dependentFsCellCode, monthlyFsCellCode);
			cellCodeToValueMap.put(dependentFsCellCode, dependentCellValue);
		}
		String expression = OemFSUtils.getExpressionReplacedByValues(context.getCodeIdentifierVsExpressionMap().get(codeIdentifier), cellCodeToValueMap);

		BigDecimal snapshotValue = null;
		if(FinancialStatementUtils.useSnapshotValuesInFS(context)){
			snapshotValue = FinancialStatementUtils.getSnapshotValue(monthlyFsCellCode, context);
		}

		if (Objects.isNull(snapshotValue)) {
			cellValue = getValueWithPrecision(OemFSUtils.getCalculatedAmount(expression), context);
		}else{
			cellValue = snapshotValue;
		}

		context.getCodeVsDetailsMap().put(monthlyFsCellCode.getCode(),
				FsCodeDetail.builder()
						.value(cellValue)
						.derived(true)
						.dependentFsCellCodes(monthlyFsCellCode.getDependentFsCellCodes())
						.build());
		context.getCodeVsValueMap().put(monthlyFsCellCode.getCode(), cellValue);
	}

	private BigDecimal getDependentCellValue(OemFsCellContext context, Map<Integer ,Map<String, Map<String, BigDecimal>>> glBalCntInfoForFS, String dependentFsCellCodeName, AccountingOemFsCellCode fSCellCode) {

		List<String> glAccountsForFsCellCode = getGlAccountsForFsCellCode(dependentFsCellCodeName, context);

		AccountingOemFsCellCode dependentFsCellCode = context.getCodeVsCellInfoMap().get(dependentFsCellCodeName);

		BigDecimal cellValue = BigDecimal.ZERO;
		Integer month = Integer.parseInt(fSCellCode.getAdditionalInfo().get(additionInfoField_month));

		for (String glAccount : glAccountsForFsCellCode) {
			BigDecimal glAccountValue = BigDecimal.ZERO;
			glAccountValue = glBalCntInfoForFS.get(month-1).get(glAccount).get(dependentFsCellCode.getValueType() + "_" + dependentFsCellCode.getDurationType());
			cellValue = cellValue.add(glAccountValue);
		}
		return cellValue;
	}

	public boolean isFutureMonth(MonthInfo activeMonthInfo, int year, int month) {
		if((activeMonthInfo.getYear() > year || (activeMonthInfo.getYear() == year && month <= activeMonthInfo.getMonth())
				|| isPostAheadMonth(activeMonthInfo.getYear(), activeMonthInfo.getMonth(), year, month))){
			return false;
		}
		return true;
	}

	private boolean isPostAheadMonth(Integer activeYear, Integer activeMonth, int year, int month) {
		if(activeMonth == 11){
			if((year == activeYear+1) && month == 0){
				return true;
			}
		}else{
			if(year == activeYear && month == activeMonth+1){
				return  true;
			}
		}
		return false;
	}

	@Override
	public List<AccountingOemFsCellCode> saveFsCellCodes(FSCellCodeListCreateDto reqDto) {

		List<String> codes = reqDto.getFsCellCodeDetails().stream().map(FSCellCodeInfoRequest::getCode).collect(Collectors.toList());
		Map< String, FSCellCodeInfoRequest> codeToFSCellCodeMap = TCollectionUtils.transformToMap(reqDto.getFsCellCodeDetails(), FSCellCodeInfoRequest::getCode);

		List<AccountingOemFsCellCode> fsCellCodeList = TCollectionUtils.nullSafeList(fsCellCodeRepo.getFsCellCodesForOemYearAndCountry(reqDto.getOemId().name(), reqDto.getYear(), reqDto.getVersion(), reqDto.getCountry()));
		Map<String, AccountingOemFsCellCode> fsCodeToCellInfoMapInDb = TCollectionUtils.transformToMap(fsCellCodeList, AccountingOemFsCellCode::getCode);

		List<AccountingOemFsCellCode> fsCodesToSave = Lists.newArrayList();
		for(String code : codes){
			if(fsCodeToCellInfoMapInDb.containsKey(code)){
				reqDto.applyFieldsToExistingFsCellCode(fsCodeToCellInfoMapInDb.get(code), codeToFSCellCodeMap.get(code));
				fsCodesToSave.add(fsCodeToCellInfoMapInDb.get(code));
			}else{
				fsCodesToSave.add(reqDto.toFsCellCode(codeToFSCellCodeMap.get(code)));
			}
		}
		fsCellCodeRepo.updateBulk(fsCodesToSave);
		return fsCellCodeRepo.findByCodesAndDealerIdAndOemIdNonDeleted(codes, reqDto.getYear(), reqDto.getOemId().name(), reqDto.getCountry());
	}

	@Override
	public void saveTemplate(OemTemplateReqDto reqDto) {

		List<OemTemplate> oemTemplateList = Lists.newArrayList();
		for (TemplateDetail templateDetail : reqDto.getTemplateDetails()) {

			OemTemplate oemTemplate =  OemTemplate.builder()
					.oemId(templateDetail.getOemId().name())
					.year(templateDetail.getYear())
					.country(templateDetail.getCountry())
					.template(templateDetail.getTemplate())
					.createdByUserId(UserContextProvider.getCurrentUserId())
					.modifiedByUserId(UserContextProvider.getCurrentUserId())
					.active(templateDetail.isActive())
					.version(templateDetail.getVersion())
					.build();

			oemTemplate.setCreatedTime(System.currentTimeMillis());
			oemTemplate.setModifiedTime(System.currentTimeMillis());

			if(templateDetail.isActive()){
				oemTemplateRepo.updateTemplatesAsInactive(templateDetail.getOemId().name(), templateDetail.getYear(), templateDetail.getCountry());
			}
			oemTemplateList.add(oemTemplate);
		}
		oemTemplateRepo.updateBulk(oemTemplateList);
	}

	@Override
	public List<OemTemplate> getOemTemplate(String oemId, Integer year) {
		return oemTemplateRepo.findActiveTemplateByOemYearAndCountry(oemId, year, dealerConfig.getDealerCountryCode());
	}

	@Override
	public AccountingOemFsCellGroup saveFsCellGroupCode(FSCellGroupCodeCreateDto reqDto) {
		AccountingOemFsCellGroup oemFSCellGroup = reqDto.toOemFSCellGroup();
		return oemFsCellGroupRepo.save(oemFSCellGroup);
	}

	@Override
	public List<AccountingOemFsCellGroup> fetchFsCellGroupCodes(String oemId, Integer year, Integer version) {
		return oemFsCellGroupRepo.findNonDeletedByOemIdYearVersionAndCountry(oemId, year, version, dealerConfig.getDealerCountryCode());
	}

	@Override
	public List<AccountingOemFsCellGroup> fetchFsCellGroupCodesInBulk(Integer year, Set<OEM> oemIds) {
		Set<String> oems = TCollectionUtils.nullSafeCollection(oemIds).stream().map(OEM::name).collect(Collectors.toSet());
		return oemFsCellGroupRepo.findByOemIdsAndYearNonDeleted(oems, year, dealerConfig.getDealerCountryCode());
	}

	@Override
	public void saveFsCellGroupCodes(FSCellGroupCodesCreateDto reqDto) {
		List<AccountingOemFsCellGroup> oemFSCellGroups = reqDto.toOemFSCellGroupList();
		oemFsCellGroupRepo.insertBulk(oemFSCellGroups);

		oemFSCellGroups.stream().forEach(fsCellGroup -> {
			AccountingOemFsCellGroupAuditEvent fsCellGroupAuditEvent = fsCellGroup.populateOemFsCellGroupAuditEvent();
			AuditEventDTO fsCellGroupEventDto = PclCodesAuditEventHelper.getAuditEvent(fsCellGroupAuditEvent);
			auditEventManager.publishEvents(fsCellGroupEventDto);
		});
	}

	@Override
	public void upsertFsCellGroupCodes(FSCellGroupCodesCreateDto reqDto) {
		List<AccountingOemFsCellGroup> oemFSCellGroups = reqDto.toOemFSCellGroupList();
		oemFsCellGroupRepo.upsertBulk(oemFSCellGroups);

		List<String> oemIds = new ArrayList<>();
		List<Integer> years = new ArrayList<>();
		List<String> cellGroupDisplayNames = new ArrayList<>();
		oemFSCellGroups.stream().forEach(fsCellGroup -> {
			oemIds.add(fsCellGroup.getOemId());
			years.add(fsCellGroup.getYear());
			cellGroupDisplayNames.add(fsCellGroup.getGroupDisplayName());
		});

		List<AccountingOemFsCellGroup> fsCellGroupList = oemFsCellGroupRepo.findByOemIdsAndGroupCodes(oemIds, years, cellGroupDisplayNames, dealerConfig.getDealerCountryCode());
		fsCellGroupList.stream().forEach(fsCellGroup -> {
			AccountingOemFsCellGroupAuditEvent fsCellGroupAuditEvent = fsCellGroup.populateOemFsCellGroupAuditEvent();
			AuditEventDTO fsCellGroupEventDto = PclCodesAuditEventHelper.getAuditEvent(fsCellGroupAuditEvent);
			auditEventManager.publishEvents(fsCellGroupEventDto);
		});
	}

	@Override
	public void migrateFsCellCodesFromGroup(OEM oem) {
		List<AccountingOemFsCellGroup> groups = oemFsCellGroupRepo.findByOemId(oem.name(), dealerConfig.getDealerCountryCode());
		List<AccountingOemFsCellCode> codes = Lists.newArrayList();
		groups.forEach(g->{
			codes.add(getFsCellCodeFromGroup(g, OemCellValueType.BALANCE, OemCellDurationType.MTD));
			codes.add(getFsCellCodeFromGroup(g, OemCellValueType.BALANCE, OemCellDurationType.YTD));
			codes.add(getFsCellCodeFromGroup(g, OemCellValueType.COUNT, OemCellDurationType.MTD));
			codes.add(getFsCellCodeFromGroup(g, OemCellValueType.COUNT, OemCellDurationType.YTD));
		});
		fsCellCodeRepo.insertBulk(codes);
	}

	@Override
	public void createFsCellCodeSnapshotForYearAndMonth(CellCodeSnapshotCreateDto dto){
		FSEntry fsEntry = fsEntryRepo.findByIdAndDealerIdWithNullCheck(dto.getFsId(), getCurrentDealerId());
		createFsCellCodeSnapshot(fsEntry, Objects.isNull(dto.getOemFsYear()) ? fsEntry.getYear():dto.getOemFsYear() , dto.getMonth(), dto.isIncludeM13(), dto.isAddM13BalInDecBalances());
	}

	@Override
	public List<OEMFsCellCodeSnapshot> getFsCellCodeSnapshots(String fsId, Integer month_1_12){
		return oemFsCellCodeSnapshotRepo.findAllSnapshotByFsIdAndMonth(fsId, month_1_12, getCurrentDealerId());
	}


	@Override
	public void createFsCellCodeSnapshot(FSEntry fsEntry, int oemFsYear, int month, boolean includeM13, boolean addM13BalInDecBalances) {
		String oemId = fsEntry.getOemId();
		int year = fsEntry.getYear();
		int oemFsVersion = fsEntry.getVersion();
		List<OEMFsCellCodeSnapshot> oemExecutiveSnapshotList = Lists.newArrayList();
		DateTime monthStart = new DateTime(year, month, 1, 0,
				0, DateTimeZone.forID(dealerConfig.getDealerTimeZoneName()));

		if (Objects.nonNull(oemFsCellCodeSnapshotRepo.findOneSnapshotByFsIdAndMonth(fsEntry.getId(), month, getCurrentDealerId()))) {
			log.warn(String.format("Snapshot already exists for oem %s, year %s, month %s", oemId, year, month));
			return;
		}
		FsCellCodeDetailsResponseDto fsCellCodeDetailsResponseDto = computeFsCellCodeDetails(oemId, oemFsYear, oemFsVersion, year, month, includeM13, fsEntry.getSiteId(), addM13BalInDecBalances);
		Map<String, FsCodeDetail> codeVsDetailsMap = fsCellCodeDetailsResponseDto.getCodeVsDetailsMap();
		if(TCollectionUtils.isEmpty(codeVsDetailsMap)){
			return;
		}
		codeVsDetailsMap.keySet().forEach(key -> {
			OEMFsCellCodeSnapshot oemFsCellCodeSnapshot = new OEMFsCellCodeSnapshot(fsEntry.getId(), year, month, getCurrentDealerId(),
					oemFsVersion, fsEntry.getSiteId(), fsEntry.getFsType());
			oemFsCellCodeSnapshot.setCreatedTime(System.currentTimeMillis());
			oemFsCellCodeSnapshot.setCode(key);
			oemFsCellCodeSnapshot.setTimestamp(monthStart.getMillis());
			oemFsCellCodeSnapshot.setOemId(oemId);
			oemFsCellCodeSnapshot.setTenantId(UserContextProvider.getCurrentTenantId());
			oemFsCellCodeSnapshot.setValue(codeVsDetailsMap.get(key).getValue());
			oemExecutiveSnapshotList.add(oemFsCellCodeSnapshot);
		});
		oemFsCellCodeSnapshotRepo.saveBulkSnapshot(oemExecutiveSnapshotList);
	}

	public void createBulkFsCellCodeSnapshot(String siteId, String oemId, int oemFsYear, int oemFsVersion, int year, int fromMonth, int toMonth, boolean includeM13, boolean addM13BalInDecBalances) {
		MonthInfo activeMonthInfo = getActiveMonthInfo();
		if(activeMonthInfo.getYear() == year && activeMonthInfo.getMonth()+1 <= toMonth) {
			throw new TBaseRuntimeException("Year and toMonth are invalid because snapshot cannot be create for current month");
		}
		FSEntry fsEntry = fsEntryRepo.findDefaultType(oemId, year, UserContextProvider.getCurrentDealerId(), siteId);
		while(year <= activeMonthInfo.getYear() && fromMonth <= toMonth) {
			createFsCellCodeSnapshot(fsEntry, oemFsYear, fromMonth, includeM13, addM13BalInDecBalances);
			log.info("Successfully created snapshot for Year {} & Month {}", year, fromMonth);
			fromMonth++;
		}
	}

	@Override
	public boolean deleteSnapshotByYearAndMonth(String siteId, String oemId, int oemFsVersion, int year, int month) {
		FSEntry fsEntry = fsEntryRepo.findDefaultType(oemId, year, UserContextProvider.getCurrentDealerId(), UserContextUtils.getSiteIdFromUserContext());
		oemFsCellCodeSnapshotRepo.deleteSnapshotByFsIdAndMonth(fsEntry.getId(), month, getCurrentDealerId());
		return true;
	}

	@Override
	public boolean deleteBulkSnapshotByYearAndMonth(String siteId, String oemId, int year, int fromMonth, int toMonth) {
		oemFsCellCodeSnapshotRepo.deleteBulkSnapshotByYearAndMonth(oemId, year, fromMonth, toMonth, getCurrentDealerId(), siteId);
		return true;
	}

	@Override
	public OemConfig getOemConfig(String oemId) {
		return oemConfigRepo.findByOemId(oemId, dealerConfig.getDealerCountryCode());
	}

	@Override
	public OemConfig saveOemConfig(OemConfigRequestDto requestDto) {
		OemConfig oemConfigInDb = oemConfigRepo.findByOemId(requestDto.getOemId().name(), requestDto.getCountry());
		if (Objects.nonNull(oemConfigInDb)) {
			oemConfigInDb.setCountry(requestDto.getCountry());
			oemConfigInDb.setXmlEnabled(requestDto.isXmlEnabled());
			oemConfigInDb.setOemLogoURL(requestDto.getOemLogoURL());
			oemConfigInDb.setSubmissionEnabled(requestDto.isSubmissionEnabled());
			oemConfigInDb.setDefaultPrecision(requestDto.getDefaultPrecision());
			oemConfigInDb.setSupportedFileFormats(requestDto.getSupportedFileFormats().stream().map(OemConfig.SupportedFileFormats::name).collect(Collectors.toList()));
			oemConfigInDb.setUseDealerLogo(requestDto.isUseDealerLogo());
			oemConfigInDb.setAdditionalInfo(requestDto.getAdditionalInfo());
			oemConfigInDb.setDownloadFileFromIntegration(requestDto.isDownloadFileFromIntegration());
			oemConfigInDb.setEnableRoundOff(requestDto.isEnableRoundOff());
			oemConfigInDb.setEnableRoundOffOffset(requestDto.isEnableRoundOffOffset());
			oemConfigInDb.setModifiedByUserId(UserContextProvider.getCurrentUserId());
			oemConfigInDb.setModifiedTime(System.currentTimeMillis());
			oemConfigInDb.setFsValidationEnabled(requestDto.isFsValidationEnabled());
			return oemConfigRepo.save(oemConfigInDb);

		} else {
			OemConfig oemConfigToBeSaved = requestDto.createOemInfo();
			return oemConfigRepo.save(oemConfigToBeSaved);
		}
	}

	@Override
	public List<OEMFsCellCodeSnapshotResponseDto> getAllOEMFsCellCodeSnapshotSummary(String siteId, String oemId, int oemFsVersion, int year, int month, int oemFsYear, boolean includeM13, boolean addM13BalInDecBalances) {
		FSEntry fsEntry = fsEntryRepo.findDefaultType(oemId, year, UserContextProvider.getCurrentDealerId(), siteId);
		List<OEMFsCellCodeSnapshotResponseDto> oemFsCellCodeSnapshotResponseDtoList = Lists.newArrayList();
		MonthInfo activeMonthInfo = getActiveMonthInfo();
		if(activeMonthInfo.getYear()>year || (activeMonthInfo.getYear() == year && activeMonthInfo.getMonth() > month-1) ) {
			List<OEMFsCellCodeSnapshot> oemFsCellCodeSnapshotList = oemFsCellCodeSnapshotRepo.findAllSnapshotByFsIdAndMonth(fsEntry.getId(), month, getCurrentDealerId());
			oemFsCellCodeSnapshotList.forEach(oemFsCellCodeSnapshot -> {
				OEMFsCellCodeSnapshotResponseDto oemFsCellCodeSnapshotResponseDto = new OEMFsCellCodeSnapshotResponseDto(
						oemFsCellCodeSnapshot.getCode(),
						oemFsCellCodeSnapshot.getValue());
				oemFsCellCodeSnapshotResponseDtoList.add(oemFsCellCodeSnapshotResponseDto);
			});
		} else {
			FsCellCodeDetailsResponseDto fsCellCodeDetailsResponseDto = computeFsCellCodeDetails(oemId, oemFsYear, oemFsVersion, year, month, includeM13, siteId, addM13BalInDecBalances);
			Map<String, FsCodeDetail> codeVsDetailsMap = fsCellCodeDetailsResponseDto.getCodeVsDetailsMap();
			codeVsDetailsMap.keySet().forEach(key -> {
				OEMFsCellCodeSnapshotResponseDto oemFsCellCodeSnapshotResponseDto = new OEMFsCellCodeSnapshotResponseDto(
						key, codeVsDetailsMap.get(key).getValue());
				oemFsCellCodeSnapshotResponseDtoList.add(oemFsCellCodeSnapshotResponseDto);
			});
		}
		return oemFsCellCodeSnapshotResponseDtoList;
	}

	@Override
	public List<OEMFsCellCodeSnapshotBulkResponseDto> getBulkOEMFsCellCodeSnapshot(String oemId, Set<String> codes, long fromTimestamp,
																				   long toTimestamp, int oemFsVersion, int oemFsYear, boolean includeM13, String siteId, boolean addM13BalInDecBalances) {
		List<OEMFsCellCodeSnapshotBulkResponseDto> oemFsCellCodeSnapshotBulkResponseDtoList = Lists.newArrayList();
		FsCellCodeDetailsResponseDto fsCellCodeDetailsResponseDto;
		DateTime dateTime = new DateTime(toTimestamp, DateTimeZone.forID(dealerConfig.getDealerTimeZoneName()));
		MonthInfo activeMonthInfo = getActiveMonthInfo();
		int fromMonth = activeMonthInfo.getMonth() +1, toMonth = dateTime.getMonthOfYear(),
				currentYear = activeMonthInfo.getYear(), toYear = dateTime.getYear();

		oemFsCellCodeSnapshotRepo.getFsCellCodeByTimestamp(fromTimestamp, toTimestamp, codes, oemId, getCurrentDealerId(),siteId )
				.forEach( record -> {
					OEMFsCellCodeSnapshotBulkResponseDto dto = new OEMFsCellCodeSnapshotBulkResponseDto(
							record.getCode(), record.getValue(), record.getTimestamp()
					);
					oemFsCellCodeSnapshotBulkResponseDtoList.add(dto);
				});

		int requiredYear = currentYear;
		int requiredMonth = fromMonth;

		List<FSEntry> fsEntries = fsEntryRepo.findFsEntriesByYearRange(oemId, requiredYear, toYear, FSType.OEM.name(), UserContextProvider.getCurrentDealerId(), siteId);
		Map<Integer, FSEntry> oemYearVsEntryMap = TCollectionUtils.transformToMap(TCollectionUtils.nullSafeList(fsEntries) , FSEntry::getYear);
		while((requiredYear < toYear ) || (requiredYear == toYear && requiredMonth <= toMonth) ) {
			FSEntry fsEntry = oemYearVsEntryMap.get(requiredYear);
			if( requiredYear == toYear && requiredMonth == toMonth) {
				fsCellCodeDetailsResponseDto = computeFsCellCodeDetails(fsEntry, toTimestamp, includeM13, addM13BalInDecBalances);
			} else {
				// passing oemFsYear and year as required Year
				fsCellCodeDetailsResponseDto  = computeFsCellCodeDetails(oemId, requiredYear, oemFsVersion,
						requiredYear, requiredMonth, includeM13, siteId, addM13BalInDecBalances);
			}

			// why dont we add end date of month here instead of starting date
			DateTime timestamp = new DateTime(requiredYear, requiredMonth, 1, 0,
					0, DateTimeZone.forID(dealerConfig.getDealerTimeZoneName()));
			Map<String, FsCodeDetail> codeVsDetailsMap = fsCellCodeDetailsResponseDto.getCodeVsDetailsMap();
			codeVsDetailsMap.keySet().forEach(key -> {
				if(codes.contains(key)) {
					OEMFsCellCodeSnapshotBulkResponseDto dto = new OEMFsCellCodeSnapshotBulkResponseDto(
							key, codeVsDetailsMap.get(key).getValue(), timestamp.getMillis());
					oemFsCellCodeSnapshotBulkResponseDtoList.add(dto);
				}
			});

			if(requiredMonth < 12){
				requiredMonth++;
			}else{
				requiredMonth = 1;
				requiredYear += 1;
			}
		}

		return oemFsCellCodeSnapshotBulkResponseDtoList;
	}

	@Override
	public List<OEMFsCellCodeSnapshotResponseDto> getOEMFsCellCodeSnapshot(String siteId, String oemId, int oemFsVersion, int year, int month,
																		   Set<String> codes, int oemFsYear, boolean includeM13, boolean addM13BalInDecBalances) {
		FSEntry fsEntry = fsEntryRepo.findDefaultType(oemId, oemFsYear, getCurrentDealerId(), UserContextUtils.getSiteIdFromUserContext());
		List<OEMFsCellCodeSnapshotResponseDto> oemFsCellCodeSnapshotResponseDtoList = Lists.newArrayList();
		MonthInfo activeMonthInfo = getActiveMonthInfo();
		if(activeMonthInfo.getYear()>year || (activeMonthInfo.getYear() == year && activeMonthInfo.getMonth() > month-1) ) {
			List<OEMFsCellCodeSnapshot> oemFsCellCodeSnapshotList = oemFsCellCodeSnapshotRepo.findSnapshotByCodesAndMonth(fsEntry.getId(), month, codes, getCurrentDealerId());
			oemFsCellCodeSnapshotList.forEach(oemFsCellCodeSnapshot -> {
				OEMFsCellCodeSnapshotResponseDto oemFsCellCodeSnapshotResponseDto = new OEMFsCellCodeSnapshotResponseDto(
						oemFsCellCodeSnapshot.getCode(),
						oemFsCellCodeSnapshot.getValue());
				oemFsCellCodeSnapshotResponseDtoList.add(oemFsCellCodeSnapshotResponseDto);
			});
		} else {
			FsCellCodeDetailsResponseDto fsCellCodeDetailsResponseDto = computeFsCellCodeDetails(oemId, oemFsYear, oemFsVersion, year, month, includeM13,siteId, addM13BalInDecBalances);
			Map<String, FsCodeDetail> codeVsDetailsMap = fsCellCodeDetailsResponseDto.getCodeVsDetailsMap();
			codeVsDetailsMap.keySet().forEach(key -> {
				if(codes.contains(key)) {
					OEMFsCellCodeSnapshotResponseDto oemFsCellCodeSnapshotResponseDto = new OEMFsCellCodeSnapshotResponseDto(
							key, codeVsDetailsMap.get(key).getValue());
					oemFsCellCodeSnapshotResponseDtoList.add(oemFsCellCodeSnapshotResponseDto);
				}
			});
		}

		return oemFsCellCodeSnapshotResponseDtoList;
	}

	@Override
	public void createFsMappingSnapshot(String fsId, int month) {
		FSEntry fsEntry = fsEntryRepo.findByIdAndDealerIdWithNullCheck(fsId, UserContextProvider.getCurrentDealerId());
		List<OemFsMappingSnapshot> fsMappingSnapshots = Lists.newArrayList();
		if(Objects.nonNull(oemFsMappingSnapshotRepo.findOneSnapshotByYearAndMonth(fsId, month, UserContextProvider.getCurrentDealerId()))) {
			throw new TBaseRuntimeException(String.format("Snapshot already exists for oem %s, year %s, month %s", fsEntry.getOemId(), fsEntry.getYear(), month));
		}
		List<OemFsMapping> currentOemMappings = oemFsMappingRepo.findMappingsByFsId(fsId, getCurrentDealerId());
		currentOemMappings.forEach(oemFsMapping -> {
			fsMappingSnapshots.add(OemFsMappingSnapshot.fromOemMapping(oemFsMapping, month, fsId));
		});
		oemFsMappingSnapshotRepo.saveBulkSnapshot(fsMappingSnapshots);
	}

	@Override
	public void createFsMappingSnapshotBulk(String siteId, String oemId, int oemFsYear, int oemFsVersion, int year, int fromMonth, int toMonth) {
		MonthInfo activeMonthInfo = getActiveMonthInfo();
		if(activeMonthInfo.getYear() == year && activeMonthInfo.getMonth()+1 <= toMonth) {
			throw new TBaseRuntimeException("Year and toMonth are invalid because snapshot cannot be create for current month");
		}
		FSEntry fsEntry = fsEntryRepo.findDefaultType(oemId, year, UserContextProvider.getCurrentDealerId(), siteId);
		while(year <= activeMonthInfo.getYear() && fromMonth <= toMonth) {
			createFsMappingSnapshot(fsEntry.getId(), fromMonth);
			log.info("Successfully created FS mapping snapshot for Year {} & Month {}", year, fromMonth);
			fromMonth++;
		}
	}

	@Override
	public void createFsMappingAndCellCodeSnapshot(int year, int month, boolean includeM13, String siteId, boolean addM13BalInDecBalances) {
		List<FSEntry> fsEntries = fsEntryRepo.findFsEntriesForYear(year, getCurrentDealerId(),siteId);
		createAllSnapshots(fsEntries, year, month, includeM13, addM13BalInDecBalances);
	}

	@Override
	public void createFsMappingAndCellCodeSnapshotForAllSites(int year, int month, boolean includeM13, boolean addM13BalInDecBalances) {
		List<FSEntry> fsEntries = fsEntryRepo.findByKeyAtDealerLevel("year", year, getCurrentDealerId());
		createAllSnapshots(fsEntries, year, month, includeM13, addM13BalInDecBalances);
	}

	private void createAllSnapshots(List<FSEntry> fsEntries, int year, int month, boolean includeM13, boolean addM13BalInDecBalances){
		log.info("Taking mapping and cell codes snapshots for site {} {}", year, month);
		Map<String, FSEntry> oemToVersion = Maps.newHashMap();
		TCollectionUtils.nullSafeList(fsEntries).forEach(fsEntry -> {
			String key = fsEntry.getId();
			if (!oemToVersion.containsKey(key)) {
				oemToVersion.put(key, fsEntry);
			}
		});
		oemToVersion.keySet().forEach(key -> {
			FSEntry fsEntry = oemToVersion.get(key);
			try {
				createFsCellCodeSnapshot(fsEntry, year, month, includeM13, addM13BalInDecBalances);
				createFsMappingSnapshot(fsEntry.getId(), month);
				log.info("Taking snapshot of mapping and cell codes finished {} {}", year, month);
			} catch (Exception e) {
				log.error("Error while creating the snapshots ", e);
			}
		});
	}

	@Override
	public OemCodeUpdateDto updateOemCode(OemCodeUpdateDto oemCodeUpdateDtos) {
		List<String> codes = TCollectionUtils.transformToList(oemCodeUpdateDtos.getCodes(), OemCodeUpdateDto.CodeUpdate::getCode);
		Map<String, OemCodeUpdateDto.CodeUpdate> codeUpdateMap = TCollectionUtils.transformToMap(oemCodeUpdateDtos.getCodes(),OemCodeUpdateDto.CodeUpdate::getCode);
		List<AccountingOemFsCellCode> accountingOemFsCellCodes = TCollectionUtils.nullSafeList(fsCellCodeRepo.findByCodesAndDealerIdAndOemIdNonDeleted(codes,
				oemCodeUpdateDtos.getYear(),oemCodeUpdateDtos.getOemId().name(), oemCodeUpdateDtos.getCountry()));
		accountingOemFsCellCodes.forEach(a->{
			a.setOemCode(codeUpdateMap.get(a.getCode()).getOemCode());
			a.setDurationType(codeUpdateMap.get(a.getCode()).getDurationType());
			if(Objects.nonNull(codeUpdateMap.get(a.getCode()).getAdditionalInfo())){
				Map<String,String> storedMap = TCollectionUtils.nullSafeMap(a.getAdditionalInfo());
				for(String key: codeUpdateMap.get(a.getCode()).getAdditionalInfo().keySet()){
					storedMap.put(key,codeUpdateMap.get(a.getCode()).getAdditionalInfo().get(key));
				}
				a.setAdditionalInfo(storedMap);
			}
		});
		fsCellCodeRepo.updateBulkOemCode(accountingOemFsCellCodes);
		return oemCodeUpdateDtos;
	}

	@Override
	public void invalidateCache() {
		accountingOemFsCellCodesCache.invalidateAll();
	}


	@Override
	public Map<Integer, Map<String, Set<String>>> getDependentGlAccounts(OEM oem, int year, int version, Set<String> cellCodes, Long tillEpoch) {

		Calendar c = TimeUtils.buildCalendar(tillEpoch);
		int tillMonth = c.get(Calendar.MONTH)+1;
		if(c.get(Calendar.YEAR) != year){
			throw new TBaseRuntimeException(FSError.invalidPayload);
		}

		List<AccountingOemFsCellCode> codes = getOemTMappingListFromCache(oem.name(), year, version, dealerConfig.getDealerCountryCode());

		Map<Integer, Map<String, Set<String>>> monthVsCellCodeToAccounts = new HashMap<>();
		Map<String, AccountingOemFsCellCode> cellCodeMap = TCollectionUtils.transformToMap(codes, AccountingOemFsCellCode::getCode);
		Map<String, Set<String>> dependentCellCodesMap = new HashMap<>();
		Map<String, Set<String>> cellCodeVsGroupCodes = new HashMap<>();
		Set<String> groupCodes = new HashSet<>();

		for(String code: new HashSet<>(cellCodes)){
			addDependentCellCodes(code, code, cellCodeMap, groupCodes, cellCodeVsGroupCodes, dependentCellCodesMap);
		}
		Map<Integer, Map<String, Set<String>>> monthVsGCodeToAccounts =
				getGlAccountsForFsCellGroupsFrMonths(oem.name(), year, tillMonth, version, groupCodes);

		for(int month=defaultMonth; month <= tillMonth; month++){
			if(TCollectionUtils.isEmpty(monthVsGCodeToAccounts.get(month))){
				monthVsCellCodeToAccounts.put(month, monthVsCellCodeToAccounts.get(defaultMonth));

			}else{
				Map<String, Set<String>> groupCodeVsGlAccountId = monthVsGCodeToAccounts.get(month);
				Map<String, Set<String>> codeVsGLAccounts = new HashMap<>();

				for(String code: cellCodeVsGroupCodes.keySet()){
					Set<String> finalGlAccountIds = new HashSet<>();
					if(TCollectionUtils.isEmpty(cellCodeVsGroupCodes.get(code))) continue;
					for(String groupCode: cellCodeVsGroupCodes.get(code)){
						Set<String> glAccountIds = groupCodeVsGlAccountId.get(groupCode);
						if(TCollectionUtils.isNotEmpty(glAccountIds)) finalGlAccountIds.addAll(glAccountIds);
					}

					codeVsGLAccounts.computeIfAbsent(code, k -> new HashSet<>()).addAll(finalGlAccountIds);
				}
				monthVsCellCodeToAccounts.put(month, codeVsGLAccounts);
			}
		}

		monthVsCellCodeToAccounts.remove(defaultMonth);

		return monthVsCellCodeToAccounts;
	}

	private void addDependentCellCodes(String parentCode, String childCode, Map<String, AccountingOemFsCellCode> cellCodeMap, Set<String> groupCodes, Map<String
			, Set<String>> cellCodeVsGroupCodes, Map<String, Set<String>> dependentCellCodesMap) {

		if(cellCodeMap.get(childCode) != null ){
			if(dependentCellCodesMap.computeIfAbsent(parentCode, k-> new HashSet<>()).contains(childCode)) return;

			AccountingOemFsCellCode childCellCode = cellCodeMap.get(childCode);

			if(childCellCode.getGroupCode() != null){
				cellCodeVsGroupCodes.computeIfAbsent(parentCode, k -> new HashSet<>()).add(childCellCode.getGroupCode());
				groupCodes.add(childCellCode.getGroupCode());
			}

			dependentCellCodesMap.get(parentCode).add(childCode);

			if(TCollectionUtils.isNotEmpty(childCellCode.getDependentFsCellCodes())){
				for(String x: childCellCode.getDependentFsCellCodes()){
					addDependentCellCodes(parentCode, x, cellCodeMap, groupCodes, cellCodeVsGroupCodes, dependentCellCodesMap);
				}
			}
		}
	}

	private AccountingOemFsCellCode getFsCellCodeFromGroup(AccountingOemFsCellGroup g, OemCellValueType valueType, OemCellDurationType durationType) {
		AccountingOemFsCellCode code = new AccountingOemFsCellCode();
		code.setOemId(g.getOemId());
		code.setDisplayName(g.getGroupDisplayName());
		code.setCode(OemFSUtils.createFsCellCode(g.getGroupCode(), valueType.name(), durationType.name()));
		code.setYear(g.getYear());
		code.setVersion(g.getVersion());
		code.setDerived(false);
		code.setValueType(valueType.name());
		code.setDurationType(durationType.name());
		code.setExpression("");
		code.setDependentFsCellCodes(Lists.newArrayList());
		code.setGroupCode(g.getGroupCode());
		code.setCreatedTime(System.currentTimeMillis());
		code.setModifiedTime(System.currentTimeMillis());
		return code;
	}

	private Map<String, TrialBalanceRow> getTrialBalanceRowForGlAccounts(MonthInfo activeMonthInfo, Integer year, Integer month, boolean includeM13, boolean addM13BalInDecBalances) {

		if (Objects.isNull(month) || Objects.isNull(year)) {
			month = activeMonthInfo.getMonth();
			year = activeMonthInfo.getYear();
		} else {
			month = month - 1;
		}

		TrialBalance trialBalance = accountingService.getTrialBalanceReportForMonthV2(
				year, month, Sets.newHashSet(), false, includeM13, addM13BalInDecBalances);
		return getGlAccountIdToTrialBMap(trialBalance);
	}

	private Map<String, TrialBalanceRow> fetchTrialBalanceRowForGlAccounts(FsReportContext context, FSEntry fsEntry) {
		Map<String, TrialBalanceRow> map;
		if(FSType.CONSOLIDATED.name().equals(fsEntry.getFsType())){
			List<TrialBalanceRow> trialBalanceRows = getTrialBalanceRowForConsolidatedFS(context, fsEntry);
			map =  TCollectionUtils.transformToMap(trialBalanceRows, TrialBalanceRow::getAccountId);
		}
		else {
			map = getGlAccountIdToTrialBMap(accountingService.getCYTrialBalanceTillDayOfMonth(
					context.getTillEpoch(),
					Sets.newConcurrentHashSet(),
					false,
					context.isIncludeM13(),
					DpUtils.doUseTbGeneratorV2VersionForFsInOem(),
					context.isAddM13BalInDecBalances()));
		}

		if(FinancialStatementUtils.isRoundOffGLBalances(context)){
			for(TrialBalanceRow row: map.values()){
				if(Objects.nonNull(row.getOpeningBalance())) row.setOpeningBalance(row.getOpeningBalance().setScale(0, BigDecimal.ROUND_HALF_UP));
				if(Objects.nonNull(row.getCurrentBalance())) row.setCurrentBalance(row.getCurrentBalance().setScale(0, BigDecimal.ROUND_HALF_UP));
				if(Objects.nonNull(row.getDebit())) row.setDebit(row.getDebit().setScale(0, BigDecimal.ROUND_HALF_UP));
				if(Objects.nonNull(row.getCredit())) row.setCredit(row.getCredit().setScale(0, BigDecimal.ROUND_HALF_UP));
			}
		}

		context.setTrialBalanceRowMap(map);

		return map;
	}


	private List<TrialBalanceRow> getTrialBalanceRowForConsolidatedFS(FsReportContext context, FSEntry fsEntry){
		List<String> dealerIds = fsEntry.getDealerIds();
		List<TrialBalanceRow> response = new ArrayList<>();
		Calendar tillEpochInUTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		tillEpochInUTC.setTimeInMillis(context.getTillEpoch());
		context.setTillEpoch(tillEpochInUTC.getTimeInMillis());
		context.setMmddyyyy(TimeUtils.getStringFromEpoch(context.getTillEpoch()));
		List<ConsolidatedFsGlBalanceReportInEpochTask> subTaskList = new ArrayList<>();
		dealerIds.forEach( dealerId -> {
			subTaskList.add(new ConsolidatedFsGlBalanceReportInEpochTask(accountingService, context,
					dealerId, UserContextProvider.getCurrentTenantId(), UserContextProvider.getCurrentUserId()));
		});
		try {
			List<Future<List<TrialBalanceRow>>> futureList =new ArrayList<>();
			subTaskList.stream().forEach( subTask -> {futureList.add(executorService.submit(subTask));});
			for(Future<List<TrialBalanceRow>> future : futureList){
				if(TCollectionUtils.isNotEmpty(future.get())){
//                    log.info("Setting result in final response {}", future.get());
					response.addAll(future.get());
				}
			}
		} catch (InterruptedException | ExecutionException e) {
			log.error("Exception During getting TrialBalanceData");
		}
		return response;
	}

	private Map<String, TrialBalanceRow> getGlAccountIdToTrialBMap(TrialBalance trialBalance) {
		List<TrialBalanceRow> trialBalanceRows = trialBalance.getAccountRows();
		return TCollectionUtils.transformToMap(trialBalanceRows, TrialBalanceRow::getAccountId);
	}

	private Map<String, List<String>> getGlAccountsForFsCellGroups(FsReportContext fsReportContext) {
		Map<String, List<String>> groupCodeVsGlAccountsMap = Maps.newHashMap();

		List<OemFsMappingSnapshot> oemFsMappingSnapshots = oemFsMappingSnapshotRepo
				.findAllSnapshotByYearAndMonth(fsReportContext.getFsId(), fsReportContext.getRequestedMonth(), getCurrentDealerId());

		List<OemFsMapping> oemFsMapping = null;
		if(TCollectionUtils.isEmpty(oemFsMappingSnapshots)){
			oemFsMapping = oemFsMappingRepo
					.getMappings(fsReportContext.getFsId(), getCurrentDealerId());
		}else{
			oemFsMapping = oemFsMappingSnapshots.stream().parallel().map(OemFsMappingSnapshot::toOemMapping).collect(Collectors.toList());
		}

		oemFsMapping.forEach(mapping -> {
			groupCodeVsGlAccountsMap.computeIfAbsent(
					mapping.getFsCellGroupCode(),
					k -> new ArrayList<String>()
			).add(mapping.getGlAccountId());
		});
		return groupCodeVsGlAccountsMap;
	}


	private Map<Integer, Map<String, Set<String>>>
	getGlAccountsForFsCellGroupsFrMonths(String oem, int year, int lastMonth, int version, Set<String> groupCodes) {

		FSEntry fsEntry = fsEntryRepo.findDefaultType(oem, year, UserContextProvider.getCurrentDealerId(), UserContextUtils.getSiteIdFromUserContext());
		List<OemFsMappingSnapshot> oemFsMappingSnapshots = oemFsMappingSnapshotRepo
				.findAllSnapshotsUntilMonth(fsEntry.getId(), lastMonth, groupCodes, getCurrentDealerId());

		Map<Integer, List<OemFsMappingSnapshot>> snapshotsByMonth = oemFsMappingSnapshots.stream().collect(Collectors.groupingBy(OemFsMappingSnapshot::getMonth));

		Map<Integer, Map<String, Set<String>>> monthVsCodeVsAccounts = new HashMap<>();

		boolean someMappingsAreEmpty = false;

		for(int i=Calendar.JANUARY+1; i<=lastMonth; i++){
			if(TCollectionUtils.isEmpty(snapshotsByMonth.get(i))){
				someMappingsAreEmpty = true;
				break;
			}
		}

		Map<String, Set<String>> groupCodeVsGlAccountsMap = Maps.newHashMap();

		if(someMappingsAreEmpty){
			List<OemFsMapping> oemFsMapping;
			oemFsMapping = oemFsMappingRepo
					.getMappingsByGroupCodes(getCurrentDealerId(), year, 1, oem, groupCodes, UserContextUtils.getSiteIdFromUserContext() );

			if(TCollectionUtils.isNotEmpty(oemFsMapping)){
				oemFsMapping.forEach(mapping -> {
					groupCodeVsGlAccountsMap.computeIfAbsent(
							mapping.getFsCellGroupCode(), k -> new HashSet<>()).add(mapping.getGlAccountId()
					);
				});
			}
		}

		for(int i=Calendar.JANUARY+1; i<=lastMonth; i++){
			if(TCollectionUtils.isNotEmpty(snapshotsByMonth.get(i))){
				Map<String, Set<String>> groupCodeVsGlAccountsMap2 = Maps.newHashMap();
				snapshotsByMonth.get(i).forEach(mapping -> {
					groupCodeVsGlAccountsMap2.computeIfAbsent(
							mapping.getFsCellGroupCode(), k -> new HashSet<>()).add(mapping.getGlAccountId()
					);
				});
				monthVsCodeVsAccounts.put(i, groupCodeVsGlAccountsMap2);
			}else{
				monthVsCodeVsAccounts.put(defaultMonth, groupCodeVsGlAccountsMap);
			}
		}

		return monthVsCodeVsAccounts;
	}

	private void computeDerivedCellValue(OemFsCellContext context) {
		AccountingOemFsCellCode fsCellCode = context.getFsCellCode();

		if(Objects.isNull(fsCellCode)){
			return ;
		}

		if (OemCellSubType.MONTHLY.name().equalsIgnoreCase(fsCellCode.getSubType())
				&& (Integer.parseInt(fsCellCode.getAdditionalInfo().get(additionInfoField_month)) != context.getRequestedMonth())) {
			return;
		}

		for(String code : fsCellCode.getDependentFsCellCodes()){
			if(context.getCodeVsDetailsMap().containsKey(code)){
				continue;
			}
			context.setFsCellCode(context.getCodeVsCellInfoMap().get(code));
			computeDerivedCellValue(context);
			context.setFsCellCode(fsCellCode);
		}

		String finalExpression = OemFSUtils.getExpressionReplacedByValues(context.getFsCellCode().getExpression(), context.getCodeVsValueMap());

		BigDecimal calculatedAmount = getValueWithPrecision(OemFSUtils.getCalculatedAmount(finalExpression), context);

		context.getCodeVsDetailsMap().put(fsCellCode.getCode(),
				FsCodeDetail.builder()
						.value(calculatedAmount)
						.derived(true)
						.dependentFsCellCodes(fsCellCode.getDependentFsCellCodes())
						.build());
		context.getCodeVsValueMap().put(fsCellCode.getCode(), calculatedAmount);
	}

	private void computeMonthlyExpressions(OemFsCellContext context) {
		AccountingOemFsCellCode fsCellCode = context.getFsCellCode();

		if(Objects.isNull(fsCellCode)){
			return ;
		}

		for(String code : fsCellCode.getDependentFsCellCodes()){
			if(context.getCodeVsDetailsMap().containsKey(code)){
				continue;
			}
			context.setFsCellCode(context.getCodeVsCellInfoMap().get(code));
			computeDerivedCellValue(context);
			context.setFsCellCode(fsCellCode);
		}

		String newExpression = OemFSUtils.getExpressionReplacedByExpression(context.getCodeVsExpressionMap()
				.get(context.getFsCellCode().getCode()), context.getCodeVsExpressionMap());

		context.getCodeVsExpressionMap().put(context.getFsCellCode().getCode(), newExpression);

		if(OemCellSubType.MONTHLY.name().equalsIgnoreCase(context.getFsCellCode().getSubType())){
			context.getCodeIdentifierVsExpressionMap().put(context.getFsCellCode().getAdditionalInfo().get(additionInfoField_codeIdentifier), newExpression);
		}
	}

	private FsCodeDetail computeNonDerivedCellValue(OemFsCellContext oemFsCellContext) {
		AccountingOemFsCellCode fsCellCode = oemFsCellContext.getFsCellCode();
		BigDecimal cellValue = BigDecimal.ZERO;
		List<OemGlAccountDetail> glAccountDetails = Lists.newArrayList();

		for (String glAccountId : oemFsCellContext.getGroupCodeVsGlAccountsMap().get(fsCellCode.getGroupCode())) {
			BigDecimal glAccountValue = BigDecimal.ZERO;
			TrialBalanceRow trialBalanceRow = oemFsCellContext.getTrialBalanceRowMap().get(glAccountId);

			if(OemCellValueType.BALANCE.name().equals(fsCellCode.getValueType())){
				if(OemCellDurationType.MTD.name().equalsIgnoreCase(fsCellCode.getDurationType())){
					glAccountValue = getMtdBalance(trialBalanceRow);
				}else{
					glAccountValue = getYtdBalance(trialBalanceRow);
				}
			}else{
				if(OemCellDurationType.MTD.name().equalsIgnoreCase(fsCellCode.getDurationType())){
					glAccountValue = getMtdCount(trialBalanceRow);
				}else{
					glAccountValue = getYtdCount(trialBalanceRow);
				}
			}
			cellValue = cellValue.add(glAccountValue);
			OemGlAccountDetail oemGlAccountDetail = OemGlAccountDetail.builder()
					.glAccountId(glAccountId)
					.value(glAccountValue)
					.build();
			glAccountDetails.add(oemGlAccountDetail);
		}
		FsCodeDetail fsCodeDetail = new FsCodeDetail();
		fsCodeDetail.setValue(getValueWithPrecision(cellValue, oemFsCellContext));
		fsCodeDetail.setGlAccountDetails(glAccountDetails);
		return fsCodeDetail;
	}

	@Override
	public AccountingOemFsCellCode saveFsCellCode(FSCellCodeCreateDto reqDto) {

		AccountingOemFsCellCode fsCellCodeInDb = fsCellCodeRepo.findByCodeOemIdYearAndCountry(reqDto.getCode(), reqDto.getYear(), reqDto.getOemId().name(), reqDto.getCountry());
		if(Objects.isNull(fsCellCodeInDb)){
			return fsCellCodeRepo.save(reqDto.toFsCellCode());
		}else{
			reqDto.applyFieldsToExistingFsCellCode(fsCellCodeInDb);
			return fsCellCodeRepo.save(fsCellCodeInDb);
		}
	}

	@Override
	public void populateGroupCodesInFsCell(OEM oem, int year, int version) {
		List<AccountingOemFsCellGroup> groups = oemFsCellGroupRepo.findNonDeletedByOemIdYearVersionAndCountry(oem.name(),year,version, dealerConfig.getDealerCountryCode());
		List<AccountingOemFsCellCode> codes = Lists.newArrayList();
		groups.forEach(g->{
			List<String> fsCellCodes = Lists.newArrayList(OemFSUtils.createFsCellCode(g.getGroupCode(), OemCellValueType.BALANCE.name(), OemCellDurationType.MTD.name()),
					OemFSUtils.createFsCellCode(g.getGroupCode(), OemCellValueType.BALANCE.name(), OemCellDurationType.YTD.name()),
					OemFSUtils.createFsCellCode(g.getGroupCode(), OemCellValueType.COUNT.name(), OemCellDurationType.MTD.name()),
					OemFSUtils.createFsCellCode(g.getGroupCode(), OemCellValueType.COUNT.name(), OemCellDurationType.YTD.name()));
			List<AccountingOemFsCellCode> accountingOemFsCellCodes = fsCellCodeRepo.findByCodesAndDealerIdAndOemIdNonDeleted(fsCellCodes,year,oem.name(), dealerConfig.getDealerCountryCode());
			TCollectionUtils.nullSafeList(accountingOemFsCellCodes).forEach(t->{
				t.setGroupCode(g.getGroupCode());
				t.setSource(g.getSource());
			});
			codes.addAll(TCollectionUtils.nullSafeList(accountingOemFsCellCodes));
		});
		fsCellCodeRepo.updateBulk(codes);
	}

	@Override
	public List<AccountingOemFsCellCode> deleteCellCodes(FsCellCodeDeleteDto dto){

		List<AccountingOemFsCellCode> cellCodesToDelete = fsCellCodeRepo
				.findByCodesAndDealerIdAndOemIdNonDeleted(
						dto.getCellCodes().stream().distinct().collect(Collectors.toList()), dto.getYear(), dto.getOemId().name(), dto.getCountry()
				);

		cellCodesToDelete.forEach(x -> x.setDeleted(true));

		fsCellCodeRepo.delete(cellCodesToDelete);
		return cellCodesToDelete;
	}

	@Override
	public List<AccountingOemFsCellCode> migrateCellCodesToYear(String oemId, int fromYear, int toYear, String country){

		List<AccountingOemFsCellCode> cellCodes1 = fsCellCodeRepo.getFsCellCodesForOemYearAndCountry(oemId, toYear, 1, country);
		if(TCollectionUtils.isNotEmpty(cellCodes1)){
			throw new TBaseRuntimeException(FSError.valuesExistForRequestedYear);
		}

		List<AccountingOemFsCellCode> cellCodes = fsCellCodeRepo.getFsCellCodesForOemYearAndCountry(oemId, fromYear, 1, country);

		if(TCollectionUtils.isEmpty(cellCodes)) return new ArrayList<>();

		for(AccountingOemFsCellCode code: cellCodes){
			code.setId(new ObjectId().toHexString());
			code.setCreatedTime(System.currentTimeMillis());
			code.setModifiedTime(System.currentTimeMillis());
			code.setYear(toYear);
		}
		fsCellCodeRepo.insertBulk(cellCodes);

		return fsCellCodeRepo.getFsCellCodesForOemYearAndCountry(oemId, toYear, 1, country);
	}

	@Override
	public List<AccountingOemFsCellGroup> migrateGroupsCodesToYear(String oemId, int fromYear, int toYear, String country){
		List<AccountingOemFsCellGroup> groupCodes1 = oemFsCellGroupRepo.findByOemId(oemId, toYear, country);

		if(TCollectionUtils.isNotEmpty(groupCodes1)){
			throw new TBaseRuntimeException(FSError.valuesExistForRequestedYear);
		}

		List<AccountingOemFsCellGroup> groupCodes = oemFsCellGroupRepo.findByOemId(oemId, fromYear, country);

		if(TCollectionUtils.isEmpty(groupCodes)) return new ArrayList<>();

		List<AccountingOemFsCellGroup> migratedGroupCodes = new ArrayList<>();
		for(AccountingOemFsCellGroup group: groupCodes){
			AccountingOemFsCellGroup clonedCellGroup = null;
			try {
				clonedCellGroup = (AccountingOemFsCellGroup) group.clone();
				AccountingOemFsCellGroup.updateInfoForClonedCellGroup(clonedCellGroup);
				clonedCellGroup.setYear(toYear);
				migratedGroupCodes.add(clonedCellGroup);
			} catch (CloneNotSupportedException e) {
				log.error(e.getMessage());
			}
		}
		oemFsCellGroupRepo.insertBulk(migratedGroupCodes);

		return oemFsCellGroupRepo.findByOemId(oemId, toYear, dealerConfig.getDealerCountryCode());
	}

	@Override
	public void createSnapshotsForMapping(MappingSnapshotDto dto){
		FSEntry fsEntry = fsEntryRepo.findByIdAndDealerIdWithNullCheck(dto.getFsId(), UserContextProvider.getCurrentDealerId());
		List<OemFsMappingSnapshot> fsMappingSnapshots = Lists.newArrayList();

		if(Objects.nonNull(oemFsMappingSnapshotRepo.findOneSnapshot(dto.getFsId(), dto.getSnapshotMonths(), getCurrentDealerId()))) {
			throw new TBaseRuntimeException("Snapshots already exists for one or more requested months!");
		}

		List<OemFsMapping> currentOemMappings = oemFsMappingRepo.findMappingsByFsId(dto.getFsId(), getCurrentDealerId());

		for(int sMonth: dto.getSnapshotMonths()){
			currentOemMappings.forEach(oemFsMapping -> {
				OemFsMappingSnapshot snapshot = oemFsMapping.toSnapshot();
				snapshot.setMonth(sMonth);
				snapshot.setYear(dto.getSnapshotYear());
				snapshot.setFsId(dto.getFsId());
				snapshot.setTenantId(UserContextProvider.getCurrentTenantId());
				fsMappingSnapshots.add(snapshot);
			});
		}

		log.info("{} OemFsMapping snapshots created", fsMappingSnapshots.size());
		oemFsMappingSnapshotRepo.saveBulkSnapshot(fsMappingSnapshots);
	}

	@Override
	public void deleteMappingSnapshots(MappingSnapshotDto dto){
		oemFsMappingSnapshotRepo.deleteSnapshots(dto.getFsId(), dto.getSnapshotMonths(), getCurrentDealerId());
	}

	@Override
	public List<AccountingOemFsCellGroup> deleteGroupCodes(String oemId, int year, List<String> groupDisplayNames, int version, String country){
		List<AccountingOemFsCellGroup> cellGroups = oemFsCellGroupRepo.findNonDeletedByOemIdYearVersionAndCountry(oemId, year, version, country);
		List<AccountingOemFsCellGroup> codesToRemove = cellGroups.stream().filter(x -> groupDisplayNames.contains(x.getGroupDisplayName())).collect(Collectors.toList());
		oemFsCellGroupRepo.delete(codesToRemove);
		return codesToRemove;
	}


	private BigDecimal getYtdBalance(TrialBalanceRow trialBalanceRow) {
		if(Objects.nonNull(trialBalanceRow)) {
			return trialBalanceRow.getCurrentBalance();
		}else {
			return BigDecimal.ZERO;
		}
	}

	private BigDecimal getMtdBalance(TrialBalanceRow trialBalanceRow) {
		if(Objects.nonNull(trialBalanceRow)) {
			return trialBalanceRow.getDebit().subtract(trialBalanceRow.getCredit());
		}else{
			return BigDecimal.ZERO;
		}
	}

	private BigDecimal getYtdCount(TrialBalanceRow trialBalanceRow) {
		if(Objects.nonNull(trialBalanceRow)) {
			return BigDecimal.valueOf(trialBalanceRow.getYtdCount());
		}else {
			return BigDecimal.ZERO;
		}
	}

	private BigDecimal getMtdCount(TrialBalanceRow trialBalanceRow) {
		if(Objects.nonNull(trialBalanceRow)) {
			return BigDecimal.valueOf(trialBalanceRow.getCount());
		}else{
			return BigDecimal.ZERO;
		}
	}

	private OEMFinancialMapping fromDto(OEMMappingDto dto, OemMappingRequestDto mappingRequest){
		OEMFinancialMapping mapping = OEMFinancialMapping.builder()
				.glAccountId(dto.getGlAccountId())
				.glAccountDealerId(dto.getGlAccountDealerId())
				.oemAccountNumber(dto.getOemAccountNumber())
				.fsId(mappingRequest.getFsId())
				.dealerId(getCurrentDealerId())
				.tenantId(UserContextProvider.getCurrentTenantId())
				.build();

		String id = dto.getId();
		if(id == null){
			id = UUID.randomUUID().toString();
		}

		mapping.setId(id);
		return  mapping;
	}

	private FsCodeDetail computeNonDerivedForMemo(OemFsCellContext oemFsCellContext) {
		BigDecimal value = BigDecimal.ZERO;
		String stringValue = null;
		String memoKey = oemFsCellContext.getFsCellCode().getAdditionalInfo().get("memoKey");
		if(TStringUtils.isNotBlank(memoKey)){
			MemoWorksheet memoWorksheet = oemFsCellContext.getMemoKeyToValueMap().get(memoKey);
			if(Objects.nonNull(memoWorksheet)){
				MemoValue memoValue = memoWorksheet.getValues().get(oemFsCellContext.getRequestedMonth() - 1);
				if(OemCellValueType.DATE.name().equals(oemFsCellContext.getFsCellCode().getValueType())){
					stringValue = TimeUtils.getStringFromEpoch(memoValue.getMtdValue().longValue(), "MMddyyyy");
					value = memoValue.getMtdValue();
				}else if(OemCellDurationType.YTD.name().equalsIgnoreCase(oemFsCellContext.getFsCellCode().getDurationType())){
					value = memoValue.getYtdValue();
				}else{
					value = memoValue.getMtdValue();
				}
			}
		}

		return  FsCodeDetail.builder()
				.stringValue(stringValue)
				.value(getValueWithPrecision(value, oemFsCellContext))
				.derived(false)
				.build();
	}

	private FsCodeDetail computeNonDerivedForHC(OemFsCellContext oemFsCellContext) {
		BigDecimal value = BigDecimal.ZERO;
		String department = oemFsCellContext.getFsCellCode().getAdditionalInfo().get("department");
		String position = oemFsCellContext.getFsCellCode().getAdditionalInfo().get("position");
		if(TStringUtils.isNotBlank(department) && TStringUtils.isNotBlank(position)){
			String hcKey = department+"_"+position;
			if(TStringUtils.isNotBlank(hcKey)){
				HCWorksheet hcWorksheet = oemFsCellContext.getHcKeyValueMap().get(hcKey);
				if(Objects.nonNull(hcWorksheet)){
					value = hcWorksheet.getValues().get(oemFsCellContext.getRequestedMonth() - 1).getValue();
				}
			}
		}

		return  FsCodeDetail.builder()
				.value(getValueWithPrecision(value, oemFsCellContext))
				.derived(false)
				.build();
	}

	private YearMonth getFYStartDetails(int requestedYear, int requestedMonth, int fiscalStartMonth){

		log.info("requestedYear {} reqMonth {} FYStartMonth {}", requestedYear, requestedMonth, fiscalStartMonth);

		int fiscalStartYear = requestedYear;

		if(requestedMonth < fiscalStartMonth){
			fiscalStartYear = fiscalStartYear-1;
		}

		return YearMonth.of(fiscalStartYear, Month.of(fiscalStartMonth+1));
	}

	private void updateMemoWorksheetsByFY(List<MemoWorksheet> sheets, FsReportContext fsReportContext) {

		//log.info("actual memoSheets {}", JsonUtil.toJson(sheets));

		if(sheets.size() <= 0 || FieldType.LABOR_RATE.name().equals(sheets.get(0).getFieldType())
				|| FieldType.PERCENTAGE.name().equals(sheets.get(0).getFieldType())) return;

		MemoWorksheet reqYearMemo = sheets.get(0);

		if(sheets.size() > 1 && sheets.get(1).getYear() == fsReportContext.getRequestedYear()) reqYearMemo = sheets.get(1);

		MemoValue[] memoValues = new MemoValue[24];

		int fromYear = fsReportContext.getFromYear();
		int fromMonth = fsReportContext.getFromMonth();

		for(MemoWorksheet memoSheet: sheets){
			if(fromYear == memoSheet.getYear()){
				for(MemoValue val: memoSheet.getValues()){
					memoValues[val.getMonth()-1] = val;
				}
			}else{
				for(MemoValue val: memoSheet.getValues()){
					memoValues[11 + val.getMonth()] = val;
				}
			}
		}

		for(int i=0; i<24; i++){
			if(memoValues[i] == null){
				memoValues[i] = new MemoValue(i%12, BigDecimal.ZERO, BigDecimal.ZERO);
			}
		}

		int curPos = fromMonth - 1;

		Calendar c = Calendar.getInstance();

		c.set(Calendar.MONTH, fromMonth-1);

		BigDecimal currentYtd = BigDecimal.ZERO;

		MemoValue[] fyMemoValues = new MemoValue[12];

		while(true){

			currentYtd = currentYtd.add(memoValues[curPos].getMtdValue());
			memoValues[curPos].setYtdValue(currentYtd);
			fyMemoValues[curPos%12] = memoValues[curPos];

			if(c.get(Calendar.MONTH) == fsReportContext.getRequestedMonth() - 1){
				break;
			}

			c.roll(Calendar.MONTH, 1);
			curPos += 1;
		}


		for(int i= 0; i < 12; i++){
			if(fyMemoValues[i] == null){
				fyMemoValues[i] =  new MemoValue(i, BigDecimal.ZERO, BigDecimal.ZERO);
			}
		}

		reqYearMemo.setValues(Arrays.asList(fyMemoValues));
		//log.info("updated fyMemoValues {}", JsonUtil.toJson(fyMemoValues));
	}

	private String getDefaultPrecision(String oemId, OemConfig oemConfig){
		if(Objects.isNull(oemConfig)) return null;

		return oemConfig.getDefaultPrecision();
	}

	private boolean getRoundOffProperty(OemConfig oemConfig) {
		String globalPref = dealerRoundOffPref.getSafeGlobalValue(DEFAULT_ROUND_OFF);
		String dealerPref = dealerRoundOffPref.getSafeValueWithUserContext(DEFAULT_ROUND_OFF);
		boolean oemPref = oemConfig.isEnableRoundOff();

		log.info("dealer property loaded {}", dealerRoundOffPref.isPropertyLoadedWithUserContext());
		log.info("global property loaded {}", dealerRoundOffPref.isGlobalPropertyLoaded());

		log.info(" dealer prop value: {} tenant Id {} dealerId {}", dealerPref, getCurrentTenantId(), getCurrentDealerId());
		log.info("global prop val: {}", globalPref);

		if (oemPref || ENABLE_ROUND_OFF.equals(dealerPref) || (DEFAULT_ROUND_OFF.equals(dealerPref) && ENABLE_ROUND_OFF.equals(globalPref))) {
			log.info("Using round off for FS generation");
			return true;
		}

		return false;
	}

	private BigDecimal getValueWithPrecision(BigDecimal value, OemFsCellContext oemFsCellContext) {

		if(!oemFsCellContext.isRoundOff()) return value;

		String prec = null;

		if(oemFsCellContext.getDefaultPrecision() != null && !oemFsCellContext.getDefaultPrecision().isEmpty()){
			prec = oemFsCellContext.getDefaultPrecision();
		}

		if(TCollectionUtils.isNotEmpty(oemFsCellContext.getFsCellCode().getAdditionalInfo())){
			String cellCodePrecision = oemFsCellContext.getFsCellCode().getAdditionalInfo().get(TConstants.PRECISION);

			if(cellCodePrecision != null && !cellCodePrecision.isEmpty()){
				prec = oemFsCellContext.getFsCellCode().getAdditionalInfo().get(TConstants.PRECISION);
			}
		}

		if(Objects.isNull(prec)) return value;

		try{
			int precision = Integer.parseInt(prec);
			value = value.setScale(precision, BigDecimal.ROUND_HALF_UP);
		}catch (NumberFormatException ignored){}

		return value;
	}

	public List<FSEntry> updateSiteIdInOemMappings(List<OemFsMappingSiteIdChangesReqDto> reqDtos){
		if(TCollectionUtils.isEmpty(reqDtos)){
			log.info("request Dto is empty");
			return null;
		}
		List<FSEntry> fsEntries = Lists.newArrayList();
		for(OemFsMappingSiteIdChangesReqDto reqDto : reqDtos){
			if(TStringUtils.isBlank(reqDto.getSiteId())){
				log.info("siteId should not be empty");
				continue;
			}
			List<OemFsMapping> oemFsMappings = oemFsMappingRepo.findMappingsForOEMYearVersionByDealerIdNonDeleted(reqDto.getOemId(), reqDto.getYear(), reqDto.getVersion(), getCurrentDealerId());
			FSEntry fsEntry = fsEntryRepo.findByOemYearVersion(reqDto.getOemId(),reqDto.getYear(),reqDto.getVersion(),UserContextProvider.getCurrentDealerId());
			List<MemoWorksheet> memoWorksheetList = memoWorksheetRepo.findForOemByYearOemIdVersion(reqDto.getOemId(), reqDto.getYear(), reqDto.getVersion());
			List<HCWorksheet> hcWorksheets = hcWorksheetRepo.findByOemIdYearVersion(reqDto.getOemId(), reqDto.getYear(), reqDto.getVersion());
			List<OEMFsCellCodeSnapshot> oemFsCellCodeSnapshots = oemFsCellCodeSnapshotRepo.findAllSnapshotByYearOemIdVersion(reqDto.getOemId(), reqDto.getYear(), reqDto.getVersion(), UserContextProvider.getCurrentDealerId());
			List<OemFsMappingSnapshot> oemFsMappingSnapshots = oemFsMappingSnapshotRepo.findAllSnapshotsByYearVersionOemId(reqDto.getOemId(), reqDto.getVersion(),reqDto.getYear());

			fsEntry.setSiteId(reqDto.getSiteId());
			oemFsMappings.forEach(oemFsMapping -> { oemFsMapping.setSiteId(reqDto.getSiteId()); });
			memoWorksheetList.forEach(memoWorksheet -> { memoWorksheet.setSiteId(reqDto.getSiteId()); });
			hcWorksheets.forEach(hcWorksheet -> { hcWorksheet.setSiteId(reqDto.getSiteId()); });
			oemFsCellCodeSnapshots.forEach(oemFsCellCodeSnapshot -> { oemFsCellCodeSnapshot.setSiteId(reqDto.getSiteId()); });
			oemFsMappingSnapshots.forEach(oemFsMappingSnapshot -> { oemFsMappingSnapshot.setSiteId(reqDto.getSiteId()); });


			fsEntries.add(fsEntry);
			oemFsMappingRepo.updateBulk(oemFsMappings);
			fsEntryRepo.save(fsEntry);
			memoWorksheetRepo.updateBulk(memoWorksheetList,UserContextProvider.getCurrentDealerId());
			hcWorksheetRepo.updateBulk(hcWorksheets,UserContextProvider.getCurrentDealerId());
			oemFsCellCodeSnapshotRepo.updateSiteIdInBulk(oemFsCellCodeSnapshots,UserContextProvider.getCurrentDealerId());
			oemFsMappingSnapshotRepo.updateSiteIdInBulk(oemFsMappingSnapshots,UserContextProvider.getCurrentDealerId());

		}
		return fsEntries;
	}

	@Override
	public void migrateOemFsMappingFromOemToFSLevel(String dealerId) {
		List<FSEntry> fsEntries = fsEntryRepo.getFSEntries(dealerId);
		for(FSEntry fsEntry : TCollectionUtils.nullSafeList(fsEntries)){
			oemFsMappingRepo.updateFsIdInOemFsMapping(fsEntry);
		}
	}

	@Override
	public void migrateOemFsCellCodeSnapshotsFromOemToFSLevel(String dealerId) {
		List<FSEntry> fsEntries = fsEntryRepo.getFSEntries(dealerId);
		for(FSEntry fsEntry : TCollectionUtils.nullSafeList(fsEntries)){
			oemFsCellCodeSnapshotRepo.updateFsIdInOemFsCellCodeSnapshots(fsEntry);
		}
	}

	@Override
	public void migrateOemFsMappingSnapshotsFromOemToFSLevel(String dealerId) {
		List<FSEntry> fsEntries = fsEntryRepo.getFSEntries(dealerId);
		for(FSEntry fsEntry : TCollectionUtils.nullSafeList(fsEntries)){
			oemFsMappingSnapshotRepo.updateFsIdInOemFsMappingSnapshots(fsEntry);
		}
	}

	@Override
	public void addFsTypeInOemFsCellCodeSnapshots(String dealerId) {
		List<FSEntry> fsEntries = fsEntryRepo.getFSEntries(dealerId);
		for(FSEntry fsEntry : TCollectionUtils.nullSafeList(fsEntries)){
			oemFsCellCodeSnapshotRepo.updateFsTypeInFsCellCodeSnapshots(fsEntry);
		}
	}

	private MonthInfo getActiveMonthInfo(){
		return accountingService.getActiveMonthInfo();
	}

	private int getFiscalYearStartMonth(){
		return accountingService.getAccountingSettings().getFiscalYearStartMonth();
	}

	private Map<Integer, Map<String, Map<String, BigDecimal>>> getGlBalCntInfoForFS(FsReportDto fsrContext){
		return accountingService.getGlBalCntInfoForFS(fsrContext);
	}
}



