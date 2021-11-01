package com.tekion.accounting.fs.service.fsEntry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tekion.accounting.fs.GlobalService;
import com.tekion.accounting.fs.TConstants;
import com.tekion.accounting.fs.beans.FSEntry;
import com.tekion.accounting.fs.beans.mappings.OemFsMapping;
import com.tekion.accounting.fs.dto.fsEntry.FSEntryUpdateDto;
import com.tekion.accounting.fs.dto.fsEntry.FsEntryCreateDto;
import com.tekion.accounting.fs.dto.mappings.FsMappingInfo;
import com.tekion.accounting.fs.dto.mappings.FsMappingInfosResponseDto;
import com.tekion.accounting.fs.enums.AccountType;
import com.tekion.accounting.fs.enums.FSType;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.repos.OemFSMappingRepo;
import com.tekion.accounting.fs.service.accountingInfo.AccountingInfoService;
import com.tekion.accounting.fs.utils.DealerConfig;
import com.tekion.accounting.fs.utils.UserContextUtils;
import com.tekion.admin.beans.beansdto.DealerMasterBulkRequest;
import com.tekion.admin.beans.dealersetting.DealerMaster;
import com.tekion.as.client.AccountingClient;
import com.tekion.as.models.beans.GLAccount;
import com.tekion.clients.preference.client.PreferenceClient;
import com.tekion.core.es.common.i.ITekFilterOperator;
import com.tekion.core.es.common.impl.TekFilterRequest;
import com.tekion.core.es.common.impl.TekSearchRequest;
import com.tekion.core.es.request.ESResponse;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.TStringUtils;
import com.tekion.core.utils.UserContext;
import com.tekion.core.utils.UserContextProvider;
import com.tekion.formprintingservice.common.ESUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.tekion.core.utils.UserContextProvider.getCurrentDealerId;


@Component
@Slf4j
@RequiredArgsConstructor
public class FsEntryServiceImpl implements FsEntryService {

  public final FSEntryRepo fsEntryRepo;
  private final AccountingInfoService accountingInfoService;
  private final OemFSMappingRepo oemFSMappingRepo;
  private final GlobalService globalService;
  private final DealerConfig dealerConfig;
  private final PreferenceClient preferenceClient;
  //private final GLAccountSearchService glAccountSearchService;
  private final AccountingClient accountingClient;

  public static final String ACCOUNT_TYPE_ID = "accountTypeId";


  @Override
  public FSEntry createFSEntry(FsEntryCreateDto reqDto) {
    FSEntry newFSEntry = reqDto.createFSEntry();
    if(FSType.CONSOLIDATED.equals(reqDto.getFsType())){
      if(!UserContextUtils.areDealerIdsBelongsToSameTenant(reqDto.getDealerIds(), dealerConfig.getDealerMaster().getTenantId(), globalService)){
        throw new TBaseRuntimeException("DealerIds should belong to same tenant");
      }
    }
    List<FSEntry> existingEntries = new ArrayList<>();
    if(FSType.OEM.equals(reqDto.getFsType())){
      existingEntries = fsEntryRepo.find(reqDto.getOemId().name(), reqDto.getYear(),
              UserContextProvider.getCurrentDealerId(), reqDto.getSiteId(), reqDto.getFsType().name());
    }
    if(existingEntries.size() > 0){
      log.info("OemMappingInfo already exists for {} {}!", reqDto.getOemId(), reqDto.getYear());
      return existingEntries.get(0);
    }
    FSEntry mappingInfo =  fsEntryRepo.save(newFSEntry);
    accountingInfoService.addOem(reqDto.getOemId());
    return mappingInfo;
  }

  @Override
  public FsMappingInfosResponseDto getAllFSEntries() {
    String siteId = UserContextUtils.getSiteIdFromUserContext();
    String mainSiteId = UserContextUtils.getDefaultSiteId();
    List<FSEntry> fsEntriesFromDb;
    if (siteId.equalsIgnoreCase(mainSiteId)) {
      fsEntriesFromDb = fsEntryRepo.getFSEntries(getCurrentDealerId());
    } else {
      fsEntriesFromDb = fsEntryRepo.fetchAllByDealerIdAndSiteId(getCurrentDealerId(), siteId);
    }
    return getFSResponseInfo(fsEntriesFromDb);
  }

  @Override
  public FsMappingInfosResponseDto getFSEntry(String oemId) {
    String siteId = UserContextUtils.getSiteIdFromUserContext();
    List<FSEntry> fsEntriesFromDb;
    fsEntriesFromDb = fsEntryRepo.findByOemFsTypeDealerIdAndSiteId(oemId, FSType.OEM.name(), getCurrentDealerId(), siteId);
    return getFSResponseInfo(fsEntriesFromDb);
  }

  private FsMappingInfosResponseDto getFSResponseInfo(List<FSEntry> fsEntriesFromDb) {
    List<FsMappingInfo> fsEntriesToSend = Lists.newArrayList();
    Set<String> dealerIds = Sets.newHashSet(UserContextProvider.getCurrentDealerId());

    for(FSEntry fsEntry: fsEntriesFromDb){
      if(FSType.CONSOLIDATED.name().equals(fsEntry.getFsType())){
        dealerIds.addAll(fsEntry.getDealerIds());
      }
    }
    Map<String, Set<String>> dealerVsGlAccountIdsMap = getGlAccountMapForDealers(dealerIds);

    for (FSEntry fsEntry : fsEntriesFromDb) {
      // TODO - keep this info in collection only, it will become expensive operation if we have too many FS entry
      List<OemFsMapping> oemFsMappingList = TCollectionUtils.nullSafeList(oemFSMappingRepo.findMappingsByFsId(fsEntry.getId(), getCurrentDealerId()));

      List<String> allIncludedIds = oemFsMappingList.stream()
              .filter(mappingInfo -> TStringUtils.isNotBlank(mappingInfo.getGlAccountId()))
              .map(OemFsMapping::getGlAccountId).distinct().collect(Collectors.toList());

      long count = 0;

      Set<String> validGLAccountIds = Sets.newHashSet();
      if(FSType.CONSOLIDATED.name().equals(fsEntry.getFsType())){
        for(String dealer: fsEntry.getDealerIds()){
          validGLAccountIds.addAll(dealerVsGlAccountIdsMap.get(dealer));
        }
      }else{
        validGLAccountIds = dealerVsGlAccountIdsMap.get(UserContextProvider.getCurrentDealerId());
      }
      for (String glAccountId : allIncludedIds) {
        if (validGLAccountIds.contains(glAccountId))
          count++;
      }

      FsMappingInfo fsMappingInfo = FsMappingInfo.builder()
              .id(fsEntry.getId())
              .oemId(fsEntry.getOemId())
              .year(fsEntry.getYear())
              .version(fsEntry.getVersion())
              .siteId(fsEntry.getSiteId())
              .fsType(fsEntry.getFsType())
              .mappedAccounts((int) count)
              .dealerIds(fsEntry.getDealerIds())
              .unmappedAccounts(validGLAccountIds.size() - ((int) count))
              .lastModifiedTime(fsEntry.getModifiedTime())
              .modifiedByUserId(fsEntry.getModifiedByUserId())
              .build();

      if (TCollectionUtils.isNotEmpty(oemFsMappingList)) {
        fsMappingInfo.setModifiedByUserId(oemFsMappingList.get(0).getModifiedByUserId());
        fsMappingInfo.setLastModifiedTime(oemFsMappingList.get(0).getModifiedTime());
      }
      fsEntriesToSend.add(fsMappingInfo);
    }
    return FsMappingInfosResponseDto.builder()
            .fsMappingInfoList(fsEntriesToSend)
            .build();
  }

  @Override
  public FSEntry getFSEntryById(String id) {
    return fsEntryRepo.findByIdAndDealerId(id, UserContextProvider.getCurrentDealerId());
  }

  @Override
  public List<FSEntry> findFsEntriesForYear(String siteId, Integer year){
    return fsEntryRepo.findFsEntriesForYear(year, UserContextProvider.getCurrentDealerId(),siteId);
  }

  @Override
  public FSEntry updateFSEntry(String fsId, FSEntryUpdateDto FSEntryUpdateDto){
    FSEntry fsEntry = fsEntryRepo.findByIdAndDealerIdWithNullCheck(fsId, UserContextProvider.getCurrentDealerId());
    fsEntry = FSEntryUpdateDto.updateFsMappingInfo(fsEntry);
    return fsEntryRepo.save(fsEntry);
  }

  @Override
  public FSEntry deleteFsEntryById(String fsIdToDelete){
    FSEntry fsEntry = fsEntryRepo.findByIdAndDealerId(fsIdToDelete, getCurrentDealerId());
    if(Objects.isNull(fsEntry))
      return null;
    if(fsEntryRepo.findFSEntriesByOem(fsEntry.getOemId(), UserContextProvider.getCurrentDealerId()).size() <= 1){
      accountingInfoService.removeOem(OEM.valueOf(fsEntry.getOemId()));
    }
    fsEntry.setDeleted(true);
    fsEntry.setOemId(fsEntry.getOemId()+"_"+System.currentTimeMillis());
    fsEntry.setModifiedTime(System.currentTimeMillis());
    return fsEntryRepo.save(fsEntry);
  }

  @Override
  public List<FSEntry> findFsEntriesForYear(Integer year){
    return fsEntryRepo.findFsEntriesForDealer(year, UserContextProvider.getCurrentDealerId());
  }

  @Override
  public List<DealerMaster> getDealersDetailForConsolidatedFS(String fsId) {
    FSEntry fsEntry = fsEntryRepo.findByIdAndDealerIdWithNullCheck(fsId, UserContextProvider.getCurrentDealerId());
    if (FSType.CONSOLIDATED.name().equals(fsEntry.getFsType())){
      List<String> dealerIds = fsEntry.getDealerIds();
      DealerMasterBulkRequest dealerMasterBulkRequest = new DealerMasterBulkRequest();
      dealerMasterBulkRequest.setDealerIds(dealerIds);
      dealerMasterBulkRequest.setSelectedFields(Arrays.asList("id", "dealerName", "tenantId", "dealerDoingBusinessAsName", "timeZone"));
      return preferenceClient.getAllDealerMastersWithSelectedFields(dealerMasterBulkRequest).getData();
    }
    return Collections.emptyList();
  }

  @Override
  public FsMappingInfosResponseDto getFSEntriesBySiteId(List<String> siteIds) {
    if(TCollectionUtils.isEmpty(siteIds)){
      siteIds = Lists.newArrayList(UserContextUtils.getDefaultSiteId());
    }
    List<FSEntry> fsEntriesFromDb = fsEntryRepo.getFSEntriesBySiteId(UserContextProvider.getCurrentDealerId(), siteIds);
    return getFSResponseInfo(fsEntriesFromDb);
  }

  private Map<String, Set<String>> getGlAccountMapForDealers(Set<String> dealerIds) {
    Map<String,Set<String>> dealerVsGlAccountCountMap = Maps.newHashMap();
    if(dealerIds.size() == 1){// Assumption is: it is for current dealer
      dealerVsGlAccountCountMap.put(UserContextProvider.getCurrentDealerId(),getActiveFSAccounts());
    }else {
      UserContext oldUserContext = UserContextProvider.getContext();
      try{
        for(String dealer: dealerIds){
          try{
            UserContext dealerContext = UserContextUtils.buildUserContext(dealer);
            UserContextProvider.setContext(dealerContext);
            dealerVsGlAccountCountMap.put(dealer, getActiveFSAccounts());
          }catch (Exception e){
            log.error("Exception occurred while searching ",e);
            dealerVsGlAccountCountMap.put(dealer, Sets.newHashSet());
          }
        }
      }finally{
        UserContextProvider.setContext(oldUserContext);
      }
    }
    return dealerVsGlAccountCountMap;
  }

  private Set<String> getActiveFSAccounts() {
    TekSearchRequest tekSearchRequest = new TekSearchRequest();
    List<TekFilterRequest> tekFilterRequests = Lists.newArrayList();
    tekFilterRequests.add(ESUtil.createFilterRequest(ACCOUNT_TYPE_ID, ITekFilterOperator.NIN,Lists.newArrayList(AccountType.MEMO.name(),AccountType.EXPENSE_ALLOCATION.name())));
    TekFilterRequest activeFilter = new TekFilterRequest();
    activeFilter.setField("active");
    activeFilter.setOperator(ITekFilterOperator.IN);
    activeFilter.setValues(Lists.newArrayList(true));
    tekFilterRequests.add(activeFilter);
    tekSearchRequest.setFilters(tekFilterRequests);
    ESUtil.addDealerAndNonDeletedFilter(tekSearchRequest);
    tekSearchRequest.setPageInfo(ESUtil.createPageInfo(0, TConstants.MAX_ES_SUPPORTED_ROWS));
    LinkedHashSet<String> fields = Sets.newLinkedHashSet();
    fields.add("id");
    tekSearchRequest.setIncludeFields(fields);
    //ESResponse<GLAccount> glAccountESResponse = glAccountSearchService.defaultSearch(tekSearchRequest);
    //TODO: Review
    ESResponse<GLAccount> glAccountESResponse = accountingClient.getGLAccountList(tekSearchRequest).getData();
    Set<String> glAccountIds = Sets.newLinkedHashSet();
    if(glAccountESResponse != null){
      glAccountESResponse.getHits().forEach(glAccount -> glAccountIds.add(glAccount.getId()));
    }
    return glAccountIds;
  }

}
