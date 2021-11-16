package com.tekion.accounting.fs.service.excelGeneration;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.tekion.accounting.fs.service.common.cache.utils.CustomFieldUtils;
import com.tekion.accounting.fs.service.common.excelGeneration.dto.ESReportCallbackDto;
import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.beans.common.AccountingOemFsCellGroup;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.beans.mappings.OemFsMapping;
import com.tekion.accounting.fs.service.common.cache.CustomFieldConfig;
import com.tekion.accounting.fs.service.common.cache.dtos.OptionMinimal;
import com.tekion.accounting.fs.common.utils.*;
import com.tekion.accounting.fs.enums.AccountType;
import com.tekion.accounting.fs.common.enums.CustomFieldType;
import com.tekion.accounting.fs.enums.FSType;
import com.tekion.accounting.fs.service.common.excelGeneration.dto.financialStatement.OEMMappingRequestDto;
import com.tekion.accounting.fs.service.common.excelGeneration.enums.ExcelReportType;
import com.tekion.accounting.fs.service.common.excelGeneration.reportRows.OEMMappingReportRow;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.repos.OemFSMappingRepo;
import com.tekion.accounting.fs.repos.OemFsCellGroupRepo;
import com.tekion.accounting.fs.service.accountingService.AccountingService;
import com.tekion.as.models.beans.GLAccount;
import com.tekion.as.models.beans.TrialBalanceRow;
import com.tekion.as.models.dto.MonthInfo;
import com.tekion.core.es.common.i.ITekFilterOperator;
import com.tekion.core.es.common.impl.TekFilterRequest;
import com.tekion.core.es.common.impl.TekSearchRequest;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.TStringUtils;
import com.tekion.core.utils.UserContext;
import com.tekion.core.utils.UserContextProvider;
import com.tekion.formprintingservice.common.ESUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.tekion.accounting.fs.common.TConstants.BLANK_STRING;
import static com.tekion.accounting.fs.common.TConstants.STATUS;
import static com.tekion.accounting.fs.service.compute.models.OemFsMappingSimilarToUI.MAPPED;
import static com.tekion.accounting.fs.service.compute.models.OemFsMappingSimilarToUI.UNMAPPED;
import static com.tekion.accounting.fs.common.enums.CustomFieldType.DEPARTMENT;
import static com.tekion.core.utils.TStringUtils.isBlank;
import static com.tekion.core.utils.TStringUtils.isNotBlank;
import static com.tekion.core.utils.UserContextProvider.getCurrentDealerId;

@Component
@RequiredArgsConstructor
@Slf4j
public class OEMMappingExcelHelperService {


	private final OemFSMappingRepo oemFSMappingRepo;
	private final OemFsCellGroupRepo oemFsCellGroupRepo;
	private final CustomFieldConfig customFieldConfig;
	private final FSEntryRepo fsEntryRepo;
	private final DealerConfig dealerConfig;
	private final AccountingService accountingService;
//	private final TrialBalanceService trialBalanceService;
//	private final MonthCloseService monthCloseService;
//	private final GLAccountRepository glAccountRepository;
//	private final GLAccountSearchService glAccountSearchService;

	public List<OEMMappingReportRow> getExportableReportRows(ExcelReportType reportType, OEMMappingExcelReportContext context) {
		populateDefaultsInContext(context);
		populateLookUpDetailsInContext(context);
		List<OEMMappingReportRow> oemMappingReportRows = createOEMMappingReportRows(context);
		return applySearchAndFiltersOnReportRows(context, oemMappingReportRows);
	}

	public List<OEMMappingReportRow> createOEMMappingReportRows(OEMMappingExcelReportContext context){
		String mappingSeperationDelimeter = ",";
		List<OEMMappingReportRow> oemMappingReportRows = new ArrayList<>();
		Map<String, TrialBalanceRow> trialBalanceRowMap = context.getTrialBalanceRowMap();
		Map<String,List<String>> idOemFsMappings = context.getGlIdOemFsMappingsMap();
		for(GLAccount glAccount : TCollectionUtils.nullSafeList(context.getGlAccountList())){
			String glAccountId = glAccount.getId();

			TrialBalanceRow trialBalanceRow = new TrialBalanceRow();
			trialBalanceRow.setAccountNumber(glAccount.getAccountNumber());
			trialBalanceRow.setAccountId(glAccountId);
			trialBalanceRow.setAccountTypeId(glAccount.getAccountTypeId());
			if(trialBalanceRowMap.containsKey(glAccountId) && Objects.nonNull(trialBalanceRowMap.get(glAccountId))){
				trialBalanceRow = trialBalanceRowMap.get(glAccountId);
			}
			OEMMappingReportRow reportRow = new OEMMappingReportRow();
			reportRow.setFranchise(context.getDealerIdToDealerNameMap().get(glAccount.getDealerId()));
			reportRow.setGlAccountNumber(glAccount.getAccountNumber());
			reportRow.setGlAccountName(glAccount.getAccountName());
			reportRow.setAccountStatus(glAccount.isActive()? OEMMappingReportRow.ACTIVE : OEMMappingReportRow.INACTIVE);
			reportRow.setAccountType(trialBalanceRow.getAccountType());
			reportRow.setDepartment("");
			reportRow.setYtdBalance(trialBalanceRow.getCurrentBalance());
			reportRow.setMtdBalance(trialBalanceRow.getCurrentBalance().subtract(trialBalanceRow.getOpeningBalance()));
			reportRow.setMtdCount(GeneralUtils.nullSafeLong(trialBalanceRow.getCount()));
			reportRow.setYtdCount(GeneralUtils.nullSafeLong(trialBalanceRow.getYtdCount()));
			reportRow.setGroupCodes(TStringUtils.join(idOemFsMappings.get(glAccount.getId()), mappingSeperationDelimeter));
			if(idOemFsMappings.containsKey(glAccount.getId()) && TCollectionUtils.isNotEmpty(idOemFsMappings.get(glAccount.getId()))) {
				reportRow.setStatus(MAPPED);
				String mappedGroupCodes = TStringUtils.join(idOemFsMappings.get(glAccount.getId()), mappingSeperationDelimeter);
				reportRow.setGroupCodes(mappedGroupCodes);
			}else {
				reportRow.setStatus(UNMAPPED);
				reportRow.setGroupCodes(BLANK_STRING);
			}
			resolveDepartmentFromId(glAccount, context.getKeyToIdToOptionMap(),reportRow);
			oemMappingReportRows.add(reportRow);
		}
		return oemMappingReportRows;
	}

	private void populateLookUpDetailsInContext(OEMMappingExcelReportContext context) {
		OEMMappingRequestDto oemMappingRequestDto = JsonUtil.initializeFromJson(JsonUtil.toJson(context.getEsReportCallbackDto().getExtraInfoForCallback()), OEMMappingRequestDto.class);
		FSEntry fsEntry = context.getFsEntry();
		Map<String,String> oemFsGroupCodeMap = TCollectionUtils.nullSafeList(oemFsCellGroupRepo.findNonDeletedByOemIdYearVersionAndCountry(fsEntry.getOemId(), fsEntry.getYear(), fsEntry.getVersion(), dealerConfig.getDealerCountryCode()))
				.stream().collect(Collectors.toMap(AccountingOemFsCellGroup::getGroupCode, AccountingOemFsCellGroup::getGroupDisplayName));
		List<OemFsMapping> oemFsMappingList = oemFSMappingRepo.findMappingsByFsId(oemMappingRequestDto.getFsId(), getCurrentDealerId());
		Map<String,List<String>> glIdOemFsMappingsMap = new HashMap<>();
		for(OemFsMapping oemFsMapping : oemFsMappingList){
			String groupDisplayName = oemFsGroupCodeMap.get(oemFsMapping.getFsCellGroupCode());
			if(glIdOemFsMappingsMap.containsKey(oemFsMapping.getGlAccountId())){
				List<String> mappedGroupCodes = new ArrayList<>(glIdOemFsMappingsMap.get(oemFsMapping.getGlAccountId()));
				mappedGroupCodes.add(groupDisplayName);
				glIdOemFsMappingsMap.put(oemFsMapping.getGlAccountId(), mappedGroupCodes);
			} else{
				glIdOemFsMappingsMap.put(oemFsMapping.getGlAccountId(), Lists.newArrayList(groupDisplayName));
			}
		}
		context.setGlIdOemFsMappingsMap(glIdOemFsMappingsMap);
		Map<CustomFieldType, Map<String, OptionMinimal>> keyToIdToOptionMap = customFieldConfig.getKeyToIdToOptionMap();
		context.setKeyToIdToOptionMap(keyToIdToOptionMap);

		MonthInfo activeMonthInfo = accountingService.getActiveMonthInfo();
		Map<String, TrialBalanceRow> trialBalanceRowMap = getTrialBalanceRowForGlAccounts(context, activeMonthInfo, fsEntry.getYear(), oemMappingRequestDto.getOemFsMonth(), oemMappingRequestDto.isIncludeM13(), oemMappingRequestDto.isAddM13BalInDecBalances());
		context.setTrialBalanceRowMap(trialBalanceRowMap);
	}

	private void populateDefaultsInContext(OEMMappingExcelReportContext context) {
		ESReportCallbackDto esReportCallbackDto = JsonUtil.initializeFromJson(JsonUtil.toJson(context.getFetchNextBatchRequest().getOriginalPayload()), ESReportCallbackDto.class);
		context.setEsReportCallbackDto(esReportCallbackDto);
		OEMMappingRequestDto oemMappingRequestDto = JsonUtil.initializeFromJson(JsonUtil.toJson(context.getEsReportCallbackDto().getExtraInfoForCallback()), OEMMappingRequestDto.class);
		context.setOemMappingRequestDto(oemMappingRequestDto);
		FSEntry fsEntry = fsEntryRepo.findByIdAndDealerIdWithNullCheck(oemMappingRequestDto.getFsId(), getCurrentDealerId());
		Set<String> dealerIds = Sets.newHashSet(UserContextProvider.getCurrentDealerId());
		if(FSType.CONSOLIDATED.name().equals(fsEntry.getFsType())){
			dealerIds.addAll(fsEntry.getDealerIds());
		}
		context.setFsEntry(fsEntry);
		context.setIncludedDealerIds(dealerIds);
		context.setGlAccountList(getAllGlAccountsForAllIncludedDealers(dealerIds, context));
	}

	public List<GLAccount> getAllGlAccountsForAllIncludedDealers(Set<String> dealerIds, OEMMappingExcelReportContext context) {
		List<GLAccount> glAccounts = new ArrayList<>();
		UserContext oldUserContext = UserContextProvider.getContext();
		try{
			TekFilterRequest franchiseFilter = TCollectionUtils.nullSafeList(context.getOemMappingRequestDto().getFilters()).stream()
					.filter(filter -> "franchise".equalsIgnoreCase(filter.getField()))
					.collect(Collectors.toList()).get(0);
			if(franchiseFilter.getValues().size() > 0){
				dealerIds = TCollectionUtils.nullSafeList(franchiseFilter.getValues()).stream().map(Object::toString).collect(Collectors.toSet());
			}
			for(String dealerId: dealerIds){
				try{
					UserContext dealerContext = UserContextUtils.buildUserContext(dealerId);
					UserContextProvider.setContext(dealerContext);
					glAccounts.addAll(getFilteredFSAccounts(context));
					String dealerName = dealerConfig.getDealerMaster().getDealerName();
					context.getDealerIdToDealerNameMap().put(dealerId, dealerName);
				}catch (Exception e){
					log.error("Exception occurred while searching ",e);
				}
			}
		}finally{
			UserContextProvider.setContext(oldUserContext);
		}
		return glAccounts;
	}

	private List<GLAccount> getFilteredFSAccounts(OEMMappingExcelReportContext context) {
		OEMMappingRequestDto oemMappingRequestDto = context.getOemMappingRequestDto();
		TekSearchRequest tekSearchRequest = new TekSearchRequest();
		List<TekFilterRequest> tekFilterRequests = Lists.newArrayList();
		tekFilterRequests.add(ESUtil.createFilterRequest("accountTypeId", ITekFilterOperator.NIN,Lists.newArrayList(AccountType.MEMO.name(),AccountType.EXPENSE_ALLOCATION.name())));
		for(TekFilterRequest filter : oemMappingRequestDto.getFilters()){
			if("departmentId".equalsIgnoreCase(filter.getField())
					|| TConstants.ACTIVE_STATUS.equalsIgnoreCase(filter.getField())
					|| "accountTypeId".equalsIgnoreCase(filter.getField()))
				tekFilterRequests.add(TESQueryUtils.createFilter(filter.getField(), filter.getValues(), ITekFilterOperator.IN));
		}
		tekSearchRequest.setFilters(tekFilterRequests);
		ESUtil.addDealerAndNonDeletedFilter(tekSearchRequest);
		tekSearchRequest.setPageInfo(ESUtil.createPageInfo(0, TConstants.MAX_ES_SUPPORTED_ROWS));
		return accountingService.defaultSearch(tekSearchRequest).getHits();
	}

	private Map<String, TrialBalanceRow> getTrialBalanceRowForGlAccounts(OEMMappingExcelReportContext context, MonthInfo activeMonthInfo, Integer year, Integer month, boolean includeM13, boolean addM13BalInDecBalances) {
		if (Objects.isNull(month) || Objects.isNull(year)) {
			month = activeMonthInfo.getMonth();
			year = activeMonthInfo.getYear();
		} else {
			month = month - 1;
		}
		List<TrialBalanceRow> trialBalanceRows = accountingService.getConsolidatedGlBalancesForMonth(
				year, month, context.getIncludedDealerIds(), true, true, addM13BalInDecBalances);
		return TCollectionUtils.transformToMap(trialBalanceRows, TrialBalanceRow::getAccountId);
	}

	private void resolveDepartmentFromId(GLAccount glAccount, Map<CustomFieldType, Map<String, OptionMinimal>> keyToIdToOptionMap, OEMMappingReportRow reportRow){
		if(isNotBlank(glAccount.getDepartmentId())){
			String optionDisplayLabel = CustomFieldUtils.getOptionDisplayLabelWithoutCode(DEPARTMENT, glAccount.getDepartmentId(), "", keyToIdToOptionMap);
			reportRow.setDepartment(isBlank(optionDisplayLabel)? "" : optionDisplayLabel);
		}
	}

	private List<OEMMappingReportRow> applySearchAndFiltersOnReportRows(OEMMappingExcelReportContext context, List<OEMMappingReportRow> oemMappingReportRows) {
		OEMMappingRequestDto oemMappingRequestDto = context.getOemMappingRequestDto();
		List<OEMMappingReportRow> oemMappingReportRowsFiltered = Lists.newArrayList();
		for (TekFilterRequest filter : TCollectionUtils.nullSafeList(oemMappingRequestDto.getFilters())){
			if (filter.getValues().size() > 0) {
				if (STATUS.equalsIgnoreCase(filter.getField())){
					oemMappingReportRows = oemMappingReportRows.stream().filter(row -> filter.getValues().contains(row.getStatus())).collect(Collectors.toList());
				}
			}
		}
		for(OEMMappingReportRow oemMappingReportRow : TCollectionUtils.nullSafeList(oemMappingReportRows)){
			if (doesMatchSearchFilter(TStringUtils.nullSafeString(oemMappingRequestDto.getSearchText()), oemMappingReportRow)){
				oemMappingReportRowsFiltered.add(oemMappingReportRow);
			}
		}
		return oemMappingReportRowsFiltered;
	}

	boolean doesMatchSearchFilter(String searchText, OEMMappingReportRow oemMappingReportRow) {
		if(TStringUtils.isBlank(searchText) ) {
			return true;
		}
		return StringUtils.containsIgnoreCase(oemMappingReportRow.getGlAccountNumber(), searchText)
				|| StringUtils.containsIgnoreCase(oemMappingReportRow.getGlAccountName(), searchText);
	}
}
