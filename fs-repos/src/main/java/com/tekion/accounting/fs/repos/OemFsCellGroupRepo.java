package com.tekion.accounting.fs.repos;

import com.mongodb.bulk.BulkWriteUpsert;
import com.tekion.accounting.fs.beans.common.AccountingOemFsCellGroup;

import java.util.List;
import java.util.Set;

public interface OemFsCellGroupRepo {

    List<AccountingOemFsCellGroup> findByKey(String key, Object value);

    List<AccountingOemFsCellGroup> findByOemIdAndGroupDisplayNames(String oemId, List<String> groupDisplayNames, String country);

    List<AccountingOemFsCellGroup> findByKeyNonDeleted(String key, Object value);

    List<AccountingOemFsCellGroup> findByOemId(String oemId, String country);

    List<AccountingOemFsCellGroup> findNonDeletedByOemIdYearVersionAndCountry(String oemId, Integer year, Integer version, String country);

    List<AccountingOemFsCellGroup> findByOemIdsAndYearNonDeleted(Set<String> oemIds, Integer year, String country);

    AccountingOemFsCellGroup save(AccountingOemFsCellGroup bean);

    void insertBulk(List<AccountingOemFsCellGroup>beans);

    List<AccountingOemFsCellGroup> findByOemId(String oemId, int year, String country);

    List<AccountingOemFsCellGroup> findCellGroupByIds(List<String> ids);

    List<BulkWriteUpsert> delete(List<AccountingOemFsCellGroup> groupCodes);

    List<BulkWriteUpsert> upsertBulk(List<AccountingOemFsCellGroup> groupCodes);

    List<AccountingOemFsCellGroup> findByGroupCodes(String oemId, Integer year, Integer version, List<String> groupCodes, String country);

    void addCountryInOemFsCellGroupCodes();

    AccountingOemFsCellGroup findByGroupCode(String oemId, Integer year, String groupCode, String country);

    List<AccountingOemFsCellGroup> findByGroupCode(Set<String> oemIds, Set<Integer> years, Set<String> countries, Set<String> groupCodes);
}