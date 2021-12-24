package com.tekion.accounting.fs.service.fsMapping;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.beans.mappings.OemFsMapping;
import com.tekion.accounting.fs.common.utils.DealerConfig;
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

        List<OemFsMapping> oemFsMappings = oemFsMappingRepo.findMappingsByFsId(fromFsId, getCurrentDealerId());
        List<OemFsMapping> copiedOemFsMappings = new ArrayList<>();
        for(OemFsMapping oemFsMapping : TCollectionUtils.nullSafeList(oemFsMappings)){
            OemFsMapping newFsMapping;
            try {
                newFsMapping = (OemFsMapping) oemFsMapping.clone();
                OemFsMapping.updateInfoForClonedMapping(newFsMapping);
                newFsMapping.setFsId(toFsId);
                newFsMapping.setYear(targetFsEntry.getYear());
                copiedOemFsMappings.add(oemFsMapping);
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


    public List<OemFsMapping> getFsMappingsByOemIdAndGroupCodes(Integer year, List<String> groupCodes, List<String> oemIds) {
        List<FSEntry> fsEntries = TCollectionUtils.nullSafeList(fsEntryRepo.getFsEntriesByOemIds(FSType.OEM, oemIds, year, UserContextProvider.getCurrentDealerId()));
        log.info("Request received for FsMapping year {}, groupCodes {} and oemIds {}", year, groupCodes, oemIds);
        Set<String> fsIds = fsEntries.stream().map(m -> m.getId()).collect(Collectors.toSet());
        return oemFsMappingRepo.findMappingsByGroupCodeAndFsIds(groupCodes, fsIds, UserContextProvider.getCurrentDealerId());
    }

    @Override
    public void hardDeleteMappings(String fsId) {
        log.warn("hard deleting mappings for {}", fsId);
        oemFsMappingRepo.hardDeleteMappings(Collections.singletonList(fsId));
    }
}