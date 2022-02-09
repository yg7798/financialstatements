package com.tekion.accounting.fs.repos;

import com.amazonaws.services.dynamodbv2.xspec.S;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.enums.FSType;

import java.util.Collection;
import java.util.List;

public interface FSEntryRepo {

    List<FSEntry> findByKey(String key, Object value, String dealerId, String siteId);

    List<FSEntry> findByKeyAtDealerLevel(String key, Object value, String dealerId);

    FSEntry save(FSEntry accountingFSEntry);

    void updateDefaultSiteId(String dealerId);

    List<FSEntry> fetchAllByDealerIdNonDeleted(String dealerId);

    List<FSEntry> fetchAllByDealerIdAndSiteId(String dealerId, String siteId);

    List<FSEntry> findFsEntriesForYear(Integer year, String dealerId, String siteId);

    FSEntry findByOem(String oemId, int version, String dealerId);

    FSEntry findByIdAndDealerId(String id, String dealerId);

    List<FSEntry> findByIds(Collection<String> id, String dealerId);

    FSEntry findByIdAndDealerIdWithNullCheck(String id, String dealerId);

    List<FSEntry> find(String oemId, int year, String dealerId, String SiteId, String fsType);

    FSEntry findDefaultType(String oemId, int year, String dealerId, String SiteId);

    FSEntry findDefaultTypeWithoutNullCheck(String oemId, int year, String dealerId, String SiteId);

    List<FSEntry> getFSEntries(String dealerId);

    List<FSEntry> getAllFSEntriesByFsType(String fsType, String dealerId);

    List<FSEntry> findFSEntriesByOem(String oemId, String dealerId);

    void bulkUpsert(List<FSEntry> fsEntries);

    FSEntry findByOemYearVersion(String oemId, int year, int version, String dealerId);

    List<FSEntry> findByOemYearVersionAndSite(String oem, Integer year, Integer version, String dealerId, String siteId);

    List<FSEntry> findFsEntriesByYearRange(String oemId, Integer fromYear, Integer toYear, String fsType, String dealerId, String siteId);

    List<FSEntry> findFsEntriesForDealer(Integer year, String dealerId);

    List<FSEntry> findDefaultTypeFsEntriesForYear(String fsType, Integer year, String dealerId);

    List<FSEntry> findByOemFsTypeDealerIdAndSiteId(String oemId, String fsType, String dealerId, String siteId);

    List<FSEntry> getFSEntriesBySiteId(String dealerId, List<String> siteIds);


    List<FSEntry> findFsEntriesForDealer(List<Integer> year, String dealerId);

    List<FSEntry> getFsEntriesByOemIds(FSType fsType, List<String> oemIds, Integer year, String dealerId);

    List<FSEntry> getFsEntriesByOemIds(List<String> oemIds, Integer year, String dealerId);

    Long updateFsTypeForFsEntry(String fsId, String changedType);
}
