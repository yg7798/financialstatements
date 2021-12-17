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
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.UserContextProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    public List<OemFsMapping> getFsMappingsByOemIdAndGroupCodes(Integer year, List<String> groupCodes, List<String> oemIds) {
        List<FSEntry> fsEntries = TCollectionUtils.nullSafeList(fsEntryRepo.getFsEntriesByOemIds(FSType.OEM, oemIds, year, UserContextProvider.getCurrentDealerId()));
        log.info("Request received for FsMapping year {}, groupCodes {} and oemIds {}", year, groupCodes, oemIds);
        Set<String> fsIds = fsEntries.stream().map(m -> m.getId()).collect(Collectors.toSet());
        return oemFsMappingRepo.findMappingsByGroupCodeAndFsIds(groupCodes, fsIds, UserContextProvider.getCurrentDealerId());
    }
}