package com.tekion.accounting.fs.repos;

import com.mongodb.bulk.BulkWriteUpsert;
import com.tekion.accounting.fs.beans.FSEntry;
import com.tekion.accounting.fs.beans.mappings.OemFsMapping;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface OemFSMappingRepo {

    List<OemFsMapping> getMappings(String fsId, String dealerId);

    List<OemFsMapping> getMappingsByGroupCodes(
            String dealerId, Integer year, Integer version, String oemId, Collection<String> groupCodes, String siteId);

    List<BulkWriteUpsert> updateBulk(List<OemFsMapping> oemFsMappings);

    List<OemFsMapping> findByGlAccountIdAndYearIncludeDeleted(String fsId, List<String> glAccountIds, String dealerId);

    List<OemFsMapping> findByGlAccountIdAndYearNonDeleted(List<String> glAccountIds, Integer year, Integer version, String dealerId, String oemId);

    List<OemFsMapping> findMappingsByFsId(String fsId, String dealerId);

    List<OemFsMapping> findNonDeletedMappingsForOEMYearVersionByDealerIdAndSite(String oemId, Integer year, Integer version, String dealerId, String siteId);

    void softDeleteAllRecords(String dealerId, String tkOemId, Integer tkOemYear, Integer tkOemVersion);
    List<OemFsMapping> findMappingsForOEMYearVersionByDealerIdNonDeleted(String oemId, Integer year, Integer version, String dealerId);

    void delete(String tkOemId, Integer tkOemYear, Integer tkOemVersion, List<OemFsMapping> mappings, String dealerId, String siteId);

    void insertBulk(List<OemFsMapping>beans);

    List<OemFsMapping> findMappingByGroupCodes(String oemId, Integer year, Integer version, String dealerId, List<String> groupCodes, String siteId);

    List<OemFsMapping> findAllByDealerIdIncludingDeleted();

    void hardDeleteMigratedOemFsMapping(String dealerId);

    List<OemFsMapping> findInBulkByGroupCodesAndOemsAndYear(Set<String> groupCodes, Set<String> oemIds, Integer year, String dealerId, String siteId);

    void updateDefaultSiteId(String dealerId);

    void updateFsIdInOemFsMapping(FSEntry fsEntry);

}
