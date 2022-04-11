package com.tekion.accounting.fs.service.fsMapping;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.beans.mappings.OemFsMapping;
import com.tekion.accounting.fs.beans.mappings.OemFsMappingDetail;
import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.accounting.fs.common.utils.TimeUtils;
import com.tekion.accounting.fs.dto.mappings.*;
import com.tekion.accounting.fs.common.utils.OemFSUtils;
import com.tekion.accounting.fs.enums.FSType;
import com.tekion.accounting.fs.events.MappingUpdateEvent;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.repos.OemFSMappingRepo;
import com.tekion.accounting.fs.repos.OemFsCellGroupRepo;
import com.tekion.accounting.fs.service.accountingService.AccountingService;
import com.tekion.accounting.fs.service.eventing.producers.FSEventHelper;
import com.tekion.as.models.dto.MonthInfo;
import com.tekion.core.beans.TBaseMongoBean;
import com.tekion.core.beans.TResponse;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.UserContextProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tekion.accounting.fs.common.utils.CollectionUtils.isTwoSetsSame;
import static com.tekion.core.utils.UserContextProvider.getCurrentDealerId;

@Slf4j
@Service
@RequiredArgsConstructor
public class FsMappingServiceImpl implements FsMappingService {

    private final OemFSMappingRepo oemFsMappingRepo;
    private final AccountingService accountingService;
    private final OemFsCellGroupRepo oemFsCellGroupRepo;
    private final DealerConfig dealerConfig;
    private final FSEntryRepo fsEntryRepo;
    private final FSEventHelper fsEventHelper;


    @Override
    public Set<String> deleteDuplicateMappings(List<String> fsIds) {
        List<OemFsMapping> oemFsMappingList = oemFsMappingRepo.getFSEntriesByFsIdsAndDealerId(fsIds, UserContextProvider.getCurrentDealerId());
        Map<String, Map<String, Set<String>>> fsIdToGlAccountDetailMapping = Maps.newHashMap();
        Set<String> oemFsMappingToBeDeleted = Sets.newHashSet();

        for(OemFsMapping oemFsMapping: TCollectionUtils.nullSafeList(oemFsMappingList)) {
            fsIdToGlAccountDetailMapping.computeIfAbsent(oemFsMapping.getFsId(), k -> Maps.newHashMap())
                    .computeIfAbsent(oemFsMapping.getGlAccountId(), l -> Sets.newHashSet());
            Set<String> fsGroupCodes = fsIdToGlAccountDetailMapping.get(oemFsMapping.getFsId()).get(oemFsMapping.getGlAccountId());
            boolean bool = fsGroupCodes.contains(oemFsMapping.getFsCellGroupCode()) ? oemFsMappingToBeDeleted.add(oemFsMapping.getId())
                    : fsGroupCodes.add(oemFsMapping.getFsCellGroupCode());
        }
        oemFsMappingRepo.deleteOemFsMappingByIdAndDealerId(oemFsMappingToBeDeleted, UserContextProvider.getCurrentDealerId());
        log.info("FsIds {}: all duplicate mappings on oemFsMapping have been deleted {}", oemFsMappingToBeDeleted);
        return oemFsMappingToBeDeleted;
    }

    @Override
    public Set<String> deleteInvalidMappings(String fsId) {
        List<OemFsMapping> oemFsMappingList = TCollectionUtils.nullSafeList(oemFsMappingRepo.findMappingsByFsId(fsId, UserContextProvider.getCurrentDealerId()));
        Set<String> oemFsMappingToBeDeleted = Sets.newHashSet();
        if(!oemFsMappingList.isEmpty()) {
            String oemId = oemFsMappingList.get(0).getOemId();
            Integer year = oemFsMappingList.get(0).getYear();

            List<String> glAccountIdList = (TCollectionUtils.nullSafeList(accountingService.getGLAccounts(UserContextProvider.getCurrentDealerId())))
                    .stream()
                    .map(glAccount -> glAccount.getId())
                    .collect(Collectors.toList());

            List<String> fsCellCodeList = (TCollectionUtils.nullSafeList(oemFsCellGroupRepo.findByOemId(oemId, year, dealerConfig.getDealerCountryCode())))
                    .stream()
                    .map(accountingOemFsCellGroup -> accountingOemFsCellGroup.getGroupCode())
                    .collect(Collectors.toList());

            oemFsMappingList.stream().forEach(oemFsMapping -> {
                if(!TCollectionUtils.nullSafeList(glAccountIdList).contains(oemFsMapping.getGlAccountId()) || !TCollectionUtils.nullSafeList(fsCellCodeList).contains(oemFsMapping.getFsCellGroupCode()))
                    oemFsMappingToBeDeleted.add(oemFsMapping.getId());
            });

            if(!oemFsMappingToBeDeleted.isEmpty())
                oemFsMappingRepo.deleteOemFsMappingByIdAndDealerId(oemFsMappingToBeDeleted, UserContextProvider.getCurrentDealerId());
            log.info("deleted irrelevant oemFsMapping {}", oemFsMappingToBeDeleted);
        }
        return oemFsMappingToBeDeleted;
    }


    @Override
    public List<OemFsMapping> updateOemFsMapping(OemFsMappingUpdateDto requestDto) {
        Set<String> groupCodes = requestDto.getMappingsToSave().stream()
                .map(OemFsMappingDetail::getFsCellGroupCode)
                .collect(Collectors.toSet());
        groupCodes.addAll(requestDto.getMappingsToDelete().stream()
                .map(OemFsMappingDetail::getFsCellGroupCode)
                .collect(Collectors.toSet()));

        List<FSEntry> fsEntries = fsEntryRepo.findByIds(Collections.singletonList(requestDto.getFsId()), UserContextProvider.getCurrentDealerId());
        if(fsEntries.size() < 1){
            throw new TBaseRuntimeException("invalid FS id");
        }

        List<OemFsMapping> existingOemFsMappings = oemFsMappingRepo.findMappingsByGroupCodeAndFsIds(groupCodes, Collections.singletonList(requestDto.getFsId()), getCurrentDealerId());

        Map<String, List<OemFsMapping>> glAcctIdVsListOfGroupsInDbMap = Maps.newHashMap();

        existingOemFsMappings.forEach(m -> {
            glAcctIdVsListOfGroupsInDbMap.computeIfAbsent( m.getGlAccountId(), k -> Lists.newArrayList()).add(m);
        });

        List<OemFsMapping> mappingsToUpsertInDb = Lists.newArrayList();

        populateMappings(requestDto, glAcctIdVsListOfGroupsInDbMap, mappingsToUpsertInDb);

        oemFsMappingRepo.updateBulk(mappingsToUpsertInDb);

        List<OemFsMapping> updatedMappings =  getOemFsMapping(requestDto.getFsId());

        List<OemFsMapping> updatedMappingsForKafka = updatedMappings.stream().filter( x -> groupCodes.contains(x.getFsCellGroupCode())).collect(Collectors.toList());

        sendKafkaEvent(existingOemFsMappings, updatedMappingsForKafka, requestDto.getFsId(), fsEntries.get(0).getOemId());

        return updatedMappings;
    }

    private void sendKafkaEvent(List<OemFsMapping> oldOemFsMappings, List<OemFsMapping> updatedMappings, String fsId, String oemId) {
        log.info("sendKafkaEvent for mapping update");
        List<MappingUpdateEvent> mappingUpdateEvents =  getMappingUpdateEvents(oldOemFsMappings, updatedMappings, fsId, oemId) ;
        for(MappingUpdateEvent mpe: mappingUpdateEvents){
            fsEventHelper.dispatchEventForMappingUpdate(mpe);
        }
    }

    private List<MappingUpdateEvent> getMappingUpdateEvents(List<OemFsMapping> oldOemFsMappings, List<OemFsMapping> updatedMappings, String fsId, String oemId) {
        Map<String, Set<String>> oldGroupCodeVsGlAccounts = oldOemFsMappings.stream()
                .collect(Collectors.groupingBy(
                        OemFsMapping::getFsCellGroupCode,
                        Collectors.mapping(OemFsMapping::getGlAccountId, Collectors.toSet())));

        Map<String, Set<String>> newGroupCodeVsGlAccounts = updatedMappings.stream()
                .collect(Collectors.groupingBy(
                        OemFsMapping::getFsCellGroupCode,
                        Collectors.mapping(OemFsMapping::getGlAccountId, Collectors.toSet())));

        Set<String> groupCodes = new HashSet<>();
        if(TCollectionUtils.isNotEmpty(oldGroupCodeVsGlAccounts.keySet())){
            groupCodes.addAll(oldGroupCodeVsGlAccounts.keySet());
        }

        if(TCollectionUtils.isNotEmpty(newGroupCodeVsGlAccounts.keySet())){
            groupCodes.addAll(newGroupCodeVsGlAccounts.keySet());
        }

        List<MappingUpdateEvent> mappingUpdateEvents =  new ArrayList<>();


        for(String groupCode: groupCodes){
            if(!isTwoSetsSame(oldGroupCodeVsGlAccounts.get(groupCode), newGroupCodeVsGlAccounts.get(groupCode))){
                MappingUpdateEvent mpe = new MappingUpdateEvent();
                mpe.setFsId(fsId);
                mpe.setOemId(oemId);
                mpe.setGroupCode(groupCode);
                Set<String> prevGlAccounts = oldGroupCodeVsGlAccounts.get(groupCode);
                Set<String> curGlAccounts = newGroupCodeVsGlAccounts.get(groupCode);
                if(prevGlAccounts == null) {
                    prevGlAccounts = new HashSet<>();
                }
                if(curGlAccounts == null) {
                    curGlAccounts = new HashSet<>();
                }
                mpe.setPrevGlAccounts(prevGlAccounts);
                mpe.setCurrentGlAccounts(curGlAccounts);

                mappingUpdateEvents.add(mpe);
            }
        }

        return mappingUpdateEvents;
    }

    private void populateMappings(OemFsMappingUpdateDto requestDto, Map<String, List<OemFsMapping>> glAcctIdVsListOfCodesInDbMap, List<OemFsMapping> mappingsToUpsertInDb) {

        FSEntry fsEntry = fsEntryRepo.findByIdAndDealerIdWithNullCheck(requestDto.getFsId(),getCurrentDealerId());
        TCollectionUtils.nullSafeList(requestDto.getMappingsToDelete()).forEach( mappingDetailReq -> {
            List<OemFsMapping> oemFsMappingsFromDb = glAcctIdVsListOfCodesInDbMap.get(mappingDetailReq.getGlAccountId());
            Map<String, OemFsMapping> groupCodeVsOemFsMappingFromDbMap = TCollectionUtils.transformToMap(oemFsMappingsFromDb, OemFsMapping::getFsCellGroupCode);
            OemFsMapping oemFsMappingFromDb = groupCodeVsOemFsMappingFromDbMap.get(mappingDetailReq.getFsCellGroupCode());
            if(Objects.nonNull(oemFsMappingFromDb)){
                oemFsMappingFromDb.setDeleted(true);
                oemFsMappingFromDb.setModifiedTime(System.currentTimeMillis());
                oemFsMappingFromDb.setModifiedByUserId(UserContextProvider.getCurrentUserId());
                mappingsToUpsertInDb.add(oemFsMappingFromDb);
            }
        });

        TCollectionUtils.nullSafeList(requestDto.getMappingsToSave()).forEach( mappingDetailReq -> {
            String glAccountId = mappingDetailReq.getGlAccountId();
            String groupCode = mappingDetailReq.getFsCellGroupCode();
            String glAccountDealerId = mappingDetailReq.getGlAccountDealerId();
            if(FSType.CONSOLIDATED.name().equals(fsEntry.getFsType()) && StringUtils.isEmpty(glAccountDealerId)){
                throw new TBaseRuntimeException("glAccountDealerId cannot be empty for Consolidated FS mappings");
            }
            List<OemFsMapping> oemFsMappingsFromDb = TCollectionUtils.nullSafeList(glAcctIdVsListOfCodesInDbMap.get(glAccountId));
            Map<String, OemFsMapping> groupCodeVsOemFsMappingFromDbMap = TCollectionUtils.transformToMap(oemFsMappingsFromDb, OemFsMapping::getFsCellGroupCode);

            if(!groupCodeVsOemFsMappingFromDbMap.containsKey(groupCode)){
                OemFsMapping oemFsMapping =  OemFsMapping.builder()
                        .oemId(fsEntry.getOemId())
                        .fsId(fsEntry.getId())
                        .fsCellGroupCode(groupCode)
                        .glAccountId(glAccountId)
                        .dealerId(getCurrentDealerId())
                        .glAccountDealerId(glAccountDealerId)
                        .year(fsEntry.getYear())
                        .version(fsEntry.getVersion())
                        .siteId(requestDto.getSiteId())
                        .modifiedByUserId(UserContextProvider.getCurrentUserId())
                        .createdByUserId(UserContextProvider.getCurrentUserId())
                        .tenantId(UserContextProvider.getCurrentTenantId())
                        .build();
                oemFsMapping.setCreatedTime(System.currentTimeMillis());
                oemFsMapping.setModifiedTime(System.currentTimeMillis());
                mappingsToUpsertInDb.add(oemFsMapping);
                glAcctIdVsListOfCodesInDbMap.computeIfAbsent(glAccountId, k -> Lists.newArrayList()).add(oemFsMapping);
            }else{
                if(groupCodeVsOemFsMappingFromDbMap.get(groupCode).isDeleted()){
                    groupCodeVsOemFsMappingFromDbMap.get(groupCode).setDeleted(false);
                    groupCodeVsOemFsMappingFromDbMap.get(groupCode).setModifiedTime(System.currentTimeMillis());
                    groupCodeVsOemFsMappingFromDbMap.get(groupCode).setModifiedByUserId(UserContextProvider.getCurrentUserId());
                    mappingsToUpsertInDb.add(groupCodeVsOemFsMappingFromDbMap.get(groupCode));
                }
            }
        });
    }

    @Override
    public List<OemFsMapping> getOemFsMapping(String fsId) {
        return TCollectionUtils.nullSafeList(
                oemFsMappingRepo.findMappingsByFsId(fsId, getCurrentDealerId()));
    }

    @Override
    public List<OemFsMapping> getMappingsByGLAccounts(String fsId, List<String> glAccounts) {
        return oemFsMappingRepo.findByGlAccountIdAndYearIncludeDeleted(fsId, glAccounts, UserContextProvider.getCurrentDealerId());
    }

    @Override
    public List<OemFsMapping> copyFsMappings(String fromFsId, String toFsId){
        FSEntry targetFsEntry = fsEntryRepo.findByIdAndDealerId(toFsId, getCurrentDealerId());
        if(Objects.isNull(targetFsEntry)){
            throw new TBaseRuntimeException("{} is invalid fsId", toFsId);
        }

        List<OemFsMapping> oemFsMappings = TCollectionUtils.nullSafeList(oemFsMappingRepo.findMappingsByFsId(fromFsId, getCurrentDealerId()));
        if(oemFsMappings.isEmpty() || !oemFsMappings.get(0).getOemId().equalsIgnoreCase(targetFsEntry.getOemId())) {
            throw new TBaseRuntimeException("FsIds {} and {} are not compatible or invalid", fromFsId, toFsId);
        }
        List<OemFsMapping> copiedOemFsMappings = new ArrayList<>();
        for(OemFsMapping oemFsMapping : oemFsMappings){
            OemFsMapping newFsMapping;
            try {
                newFsMapping = (OemFsMapping) oemFsMapping.clone();
                OemFsMapping.updateInfoForClonedMapping(newFsMapping);
                newFsMapping.setFsId(toFsId);
                newFsMapping.setYear(targetFsEntry.getYear());
                copiedOemFsMappings.add(newFsMapping);
            } catch (CloneNotSupportedException e){
                log.error("Error while cloning OemFsMapping");
            }
        }

        if(TCollectionUtils.isNotEmpty(copiedOemFsMappings)) oemFsMappingRepo.insertBulk(copiedOemFsMappings);
        return oemFsMappingRepo.findMappingsByFsId(toFsId, getCurrentDealerId());
    }

    @Override
    public void migrateFsMappingsFromYearToYear(Integer fromYear, Integer toYear, List<String> oemIds){
        if(fromYear.equals(toYear)){
            throw new TBaseRuntimeException("years cannot be same");
        }
        List<Integer> years = new ArrayList<>();
        years.add(fromYear);
        years.add(toYear);
        List<FSEntry> fsEntries = fsEntryRepo.findFsEntriesForDealer(years, UserContextProvider.getCurrentDealerId());

        if(TCollectionUtils.isNotEmpty(oemIds)){
            fsEntries = fsEntries.stream().filter(x -> oemIds.contains(x.getOemId())).filter(y -> fromYear.equals(y.getYear()))
                    .collect(Collectors.toList());
        }

        List<FSEntry> fromFsEntries = fsEntries.stream().filter(y -> fromYear.equals(y.getYear())).collect(Collectors.toList());
        List<FSEntry> toFsEntries = fsEntries.stream().filter(y -> toYear.equals(y.getYear())).collect(Collectors.toList());

        Map<String, FSEntry> toRefVsEntry = toFsEntries.stream().collect(Collectors.toMap(FSEntry::getParentFsEntryRef, Function.identity()));

        List<OemFsMapping> fsMappings = oemFsMappingRepo.getFSEntriesByFsIdsAndDealerId(fsEntries.stream()
                .map(TBaseMongoBean::getId).collect(Collectors.toList()), UserContextProvider.getCurrentDealerId());

        Map<String, List<OemFsMapping>>  fsIdVsMappings = fsMappings.stream().collect(Collectors.groupingBy(OemFsMapping::getFsId));

        List<OemFsMapping> copiedMappings = new ArrayList<>();

        for(FSEntry fromFsEntry: fromFsEntries) {
            FSEntry toFsEntry = toRefVsEntry.get(fromFsEntry.getParentFsEntryRef());
            if(Objects.isNull(toFsEntry)){
                log.warn("{} fsEntry with parentRef {} is not present", toYear, fromFsEntry.getParentFsEntryRef());
                continue;
            }
            List<OemFsMapping> fromFSMappings1 = fsIdVsMappings.get(fromFsEntry.getId());
            List<OemFsMapping> toFSMappings1 = fsIdVsMappings.get(toFsEntry.getId());

            if(TCollectionUtils.isEmpty(fromFSMappings1) || TCollectionUtils.isNotEmpty(toFSMappings1)) {
                log.warn("Not copying mappings for parentRef {}", fromFsEntry.getParentFsEntryRef());
                continue;
            }

            for(OemFsMapping fsMapping: fromFSMappings1){
                try{
                    OemFsMapping clonedMapping = (OemFsMapping) fsMapping.clone();
                    OemFsMapping.updateInfoForClonedMapping(clonedMapping);
                    clonedMapping.setYear(toYear);
                    clonedMapping.setFsId(toFsEntry.getId());
                    copiedMappings.add(clonedMapping);
                } catch (CloneNotSupportedException e){
                    log.error(e.getMessage());
                }
            }
        }

        oemFsMappingRepo.insertBulk(copiedMappings);
        log.info("copying of mappings done for {} {} ", toYear, UserContextProvider.getCurrentDealerId());
    }


    @Override
    public List<OemFsMapping> getFsMappingsByOemIdAndGroupCodes(Integer year, List<String> groupCodes, List<String> oemIds, boolean ignoreFsType) {

        List<FSEntry> fsEntries;

        if(ignoreFsType){
            fsEntries = fsEntryRepo.getFsEntriesByOemIds(oemIds, year, UserContextProvider.getCurrentDealerId());
        }else{
            fsEntries = fsEntryRepo.getFsEntriesByOemIds(FSType.OEM, oemIds, year, UserContextProvider.getCurrentDealerId());
        }
        log.info("Request received for FsMapping year {}, groupCodes {} and oemIds {}", year, groupCodes, oemIds);
        Set<String> fsIds = fsEntries.stream().map(TBaseMongoBean::getId).collect(Collectors.toSet());
        return oemFsMappingRepo.findMappingsByGroupCodeAndFsIds(groupCodes, fsIds, UserContextProvider.getCurrentDealerId());
    }

    @Override
    public void hardDeleteMappings(String fsId) {
        log.warn("hard deleting mappings for {}", fsId);
        oemFsMappingRepo.hardDeleteMappings(Collections.singletonList(fsId));
    }

    @Override
    public void deleteMappingsByGroupCodes(List<String> groupDisplayNames, String oemId, Integer year, String country){
        if(!country.equalsIgnoreCase(dealerConfig.getDealerCountryCode())){
            log.info("dealer country {}, country from api {}", dealerConfig.getDealerCountryCode(), country);
            return;
        }
        List<OemFsMapping> mappingsToDelete = getMappingsByGroupDisplayNames(groupDisplayNames, oemId, year);
        mappingsToDelete.forEach(x -> {
            x.setModifiedTime(System.currentTimeMillis());
            x.setModifiedByUserId(UserContextProvider.getCurrentUserId());
            x.setDeleted(true);
        });

        oemFsMappingRepo.updateBulk(mappingsToDelete);
        log.info("{} mappings deleted for {} {} dealer {}", mappingsToDelete.size(), oemId, year, UserContextProvider.getCurrentDealerId());
    }

    @Override
    public void replaceGroupCodesInMappings(Map<String, String> groupDisplayNamesMap, String oemId, Integer year, String country){
        if(!country.equalsIgnoreCase(dealerConfig.getDealerCountryCode())){
            log.info("dealer country {}, country from api {}", dealerConfig.getDealerCountryCode(), country);
            return;
        }
        List<OemFsMapping> mappings = getMappingsByGroupDisplayNames(groupDisplayNamesMap.keySet(), oemId, year);
        Map<String, String> groupCodeVsDisplayName = groupDisplayNamesMap.keySet().stream().filter(Objects::nonNull).collect(Collectors.toMap(OemFSUtils::createGroupCode, x -> x ));
        mappings.forEach(x -> {
            String valGroupCode = groupDisplayNamesMap.get(groupCodeVsDisplayName.get(x.getFsCellGroupCode()));
            if(Objects.isNull(valGroupCode)) return;
            x.setFsCellGroupCode(OemFSUtils.createGroupCode(valGroupCode));
            x.setModifiedTime(System.currentTimeMillis());
            x.setModifiedByUserId(UserContextProvider.getCurrentUserId());
        });
        oemFsMappingRepo.updateBulk(mappings);
        log.info("{} mappings updated through replacing group codes for {} {} dealer {}",
                mappings.size(), oemId, year, UserContextProvider.getCurrentDealerId());

    }

    private List<OemFsMapping> getMappingsByGroupDisplayNames(Collection<String> groupDisplayNames, String oemId, Integer year){
        List<String> groupCodes = groupDisplayNames.stream().map(OemFSUtils::createGroupCode).collect(Collectors.toList());
        return getFsMappingsByOemIdAndGroupCodes(year, groupCodes, Collections.singletonList(oemId), true);
    }

    @Override
    public List<GroupCodeMappingDetails> getGLAccounts(Integer year, List<OemFsGroupCodeDetails> details) {
        List<GroupCodeMappingDetails> glAccountsResponseDetails = new ArrayList<>();
        List<String> oemIds = new ArrayList<>();
        List<String> fsIds = new ArrayList<>();

        for (OemFsGroupCodeDetails fsGroupCodeDetails : details) {
            oemIds.add(fsGroupCodeDetails.getOemId());
        }
        List<FSEntry> getFsEntries = fsEntryRepo.getFsEntriesByOemIds(FSType.OEM, oemIds, year, getCurrentDealerId());
        for (FSEntry fsEntry : TCollectionUtils.nullSafeList(getFsEntries)) {
            fsIds.add(fsEntry.getId());
        }

        List<OemFsMapping> oemFsMappings = oemFsMappingRepo.getMappingsByOemIds(fsIds, details);
        Map<String, List<OemFsMapping>> oemVsFsMappingsMap = getOemVsFsMappingsMap(oemFsMappings);

        for (OemFsGroupCodeDetails fsGroupCodeDetails : details) {
            GroupCodeMappingDetails glAcctResponse = new GroupCodeMappingDetails();
            glAcctResponse.setOemId(fsGroupCodeDetails.getOemId());
            List<OemFsMapping> mappings = oemVsFsMappingsMap.get(fsGroupCodeDetails.getOemId());

            Map<String, List<String>> groupCodeVsGLAcctMap = getGroupCodeVsGLAcctMap(mappings);
            List<GroupCodesVsGLAccounts> groupCodesVsGLAccounts = new ArrayList<>();
            for (String groupCode : groupCodeVsGLAcctMap.keySet()) {
                GroupCodesVsGLAccounts glAccounts = new GroupCodesVsGLAccounts();
                glAccounts.setGlAccounts(groupCodeVsGLAcctMap.get(groupCode));
                glAccounts.setGroupCode(groupCode);
                groupCodesVsGLAccounts.add(glAccounts);
            }

            glAcctResponse.setGroupCodesMapping(groupCodesVsGLAccounts);
            glAccountsResponseDetails.add(glAcctResponse);
        }
        return glAccountsResponseDetails;
    }

    @Override
    public List<GroupCodeMappingResponseDto> getGLAccountsForMultipleYears(List<OemFsGroupCodeDetailsRequestDto> details) {
        List<GroupCodeMappingResponseDto> glAccountsResponse = new ArrayList<>();
        List<String> oemIds = getOemIds(details);
        MonthInfo monthInfo = accountingService.getActiveMonthInfo();

        List<FSEntry> fsEntries = fsEntryRepo.getFsEntriesByOemIds(FSType.OEM, oemIds, getCurrentDealerId());
        List<FSEntry> fsEntriesForActiveYear = fsEntryRepo.getFsEntriesByOemIds(FSType.OEM, oemIds,
                monthInfo.getYear(), getCurrentDealerId());

        List<String> fsIds = getFsIds(fsEntries, oemIds);
        List<String> fsIdsForActiveYear = getFsIds(fsEntriesForActiveYear, oemIds);

        List<OemFsMapping> oemFsMappings = oemFsMappingRepo.getMappingsByOemIdsForMultipleYears(fsIds, details);
        List<OemFsMapping> oemFsMappingsForActiveYear = oemFsMappingRepo.getFSEntriesByFsIdsAndDealerId(fsIdsForActiveYear,
                getCurrentDealerId());

        Map<String, List<OemFsMapping>> oemVsFsMappingsMap = getOemVsFsMappingsMap(oemFsMappings);
        Map<String, List<OemFsMapping>> oemVsFsMappingsMapActiveYear = getOemVsFsMappingsMap(oemFsMappingsForActiveYear);
        Map<String, List<String>> groupCodeVsGLAcctMapForActiveYear = new HashMap<>();
        for (OemFsGroupCodeDetailsRequestDto groupCodeDetails : details) {
            List<Integer> years = groupCodeDetails.getYears();
            for (Integer year : years) {
                List<OemFsMapping> mappings = getMappingsByYearAndOemId(year, oemVsFsMappingsMap.get(groupCodeDetails.getOemId()));
                List<OemFsMapping> activeYearMappings = getMappingsByYearAndOemId(monthInfo.getYear(), oemVsFsMappingsMapActiveYear.get(groupCodeDetails.getOemId()));
                if (year > monthInfo.getYear()) {
                    mappings.addAll(activeYearMappings);
                }

                GroupCodeMappingResponseDto glAcctResponse = new GroupCodeMappingResponseDto();
                glAcctResponse.setOemId(groupCodeDetails.getOemId());
                glAcctResponse.setYear(year);
                Map<String, List<String>> groupCodeVsGLAcctMap = getGroupCodeVsGLAcctMap(mappings);
                groupCodeVsGLAcctMapForActiveYear = getGroupCodeVsGLAcctMap(activeYearMappings);

                List<GroupCodesVsGLAccounts> groupCodesVsGLAccounts = new ArrayList<>();
                for (String groupCode : groupCodeDetails.getGroupCodes()) {
                    GroupCodesVsGLAccounts glAccounts;
                    if (groupCodeVsGLAcctMap.containsKey(groupCode)) {
                        glAccounts = new GroupCodesVsGLAccounts();
                        glAccounts.setGlAccounts(groupCodeVsGLAcctMap.get(groupCode));
                        glAccounts.setGroupCode(groupCode);
                        groupCodesVsGLAccounts.add(glAccounts);
                    } else if (groupCodeVsGLAcctMapForActiveYear.containsKey(groupCode)) {
                        glAccounts = new GroupCodesVsGLAccounts();
                        glAccounts.setGlAccounts(groupCodeVsGLAcctMapForActiveYear.get(groupCode));
                        glAccounts.setGroupCode(groupCode);
                        groupCodesVsGLAccounts.add(glAccounts);
                    }
                }
                glAcctResponse.setGroupCodesMapping(groupCodesVsGLAccounts);
                glAccountsResponse.add(glAcctResponse);
            }
        }
        return glAccountsResponse;
    }

    private Map<String, List<String>> getGroupCodeVsGLAcctMap(List<OemFsMapping> mappings) {
        Map<String, List<String>> groupCodeVsGLAcctMap = new HashMap<>();
        for (OemFsMapping oemFsMapping : TCollectionUtils.nullSafeList(mappings)) {
            if (groupCodeVsGLAcctMap.containsKey(oemFsMapping.getFsCellGroupCode())) {
                List<String> glAcctIds = groupCodeVsGLAcctMap.get(oemFsMapping.getFsCellGroupCode());
                glAcctIds.add(oemFsMapping.getGlAccountId());
            } else {
                List<String> glAcctIds = new ArrayList<>();
                glAcctIds.add(oemFsMapping.getGlAccountId());
                groupCodeVsGLAcctMap.put(oemFsMapping.getFsCellGroupCode(), glAcctIds);
            }
        }
        return groupCodeVsGLAcctMap;
    }

    private List<String> getFsIds(List<FSEntry> fsEntries, List<String> oemIds) {
        List<String> fsIds = new ArrayList<>();
        for (FSEntry fsEntry : TCollectionUtils.nullSafeList(fsEntries)) {
            fsIds.add(fsEntry.getId());
        }
        return fsIds;
    }

    private List<String> getOemIds(List<OemFsGroupCodeDetailsRequestDto> details) {
        List<String> oemIds = new ArrayList<>();
        for (OemFsGroupCodeDetailsRequestDto fsGroupCodeDetails : details) {
            oemIds.add(fsGroupCodeDetails.getOemId());
        }
        return oemIds;
    }

    private List<OemFsMapping> getMappingsByYearAndOemId(Integer year, List<OemFsMapping> oemFsMappings) {
        List<OemFsMapping> fsMappings = new ArrayList<>();
        for (OemFsMapping mapping : TCollectionUtils.nullSafeList(oemFsMappings)) {
            if (year.equals(mapping.getYear())) {
                fsMappings.add(mapping);
            }
        }
        return fsMappings;
    }

    private Map<String, List<OemFsMapping>> getOemVsFsMappingsMap(List<OemFsMapping> oemFsMappings) {
        Map<String, List<OemFsMapping>> oemVsFsMappingsMap = new HashMap<>();
        for (OemFsMapping oemFsMapping : oemFsMappings) {
            if (oemVsFsMappingsMap.containsKey(oemFsMapping.getOemId())) {
                List<OemFsMapping> mappings = oemVsFsMappingsMap.get(oemFsMapping.getOemId());
                mappings.add(oemFsMapping);
            } else {
                List<OemFsMapping> fsMappings = new ArrayList<>();
                fsMappings.add(oemFsMapping);
                oemVsFsMappingsMap.put(oemFsMapping.getOemId(), fsMappings);
            }
        }
        return oemVsFsMappingsMap;
    }
}