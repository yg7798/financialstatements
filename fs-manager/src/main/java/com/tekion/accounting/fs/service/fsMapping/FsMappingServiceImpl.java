package com.tekion.accounting.fs.service.fsMapping;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.beans.mappings.OemFsMapping;
import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.accounting.fs.common.utils.OemFSUtils;
import com.tekion.accounting.fs.enums.FSType;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.repos.OemFSMappingRepo;
import com.tekion.accounting.fs.repos.OemFsCellGroupRepo;
import com.tekion.accounting.fs.service.accountingService.AccountingService;
import com.tekion.core.beans.TBaseMongoBean;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.UserContextProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public List<OemFsMapping> getOemFsMapping(String fsId) {
        return TCollectionUtils.nullSafeList(
                oemFsMappingRepo.findMappingsByFsId(fsId, getCurrentDealerId()));
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
}