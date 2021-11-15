package com.tekion.accounting.fs.repos;

import com.mongodb.bulk.BulkWriteUpsert;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.beans.mappings.OemFsMappingSnapshot;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface OemFsMappingSnapshotRepo {
    void saveBulkSnapshot(List<OemFsMappingSnapshot> oemFsMappingSnapshots);

    OemFsMappingSnapshot findOneSnapshotByYearAndMonth(String fsId, int month, String dealerId);

    List<OemFsMappingSnapshot> findAllSnapshotByYearAndMonth(String fsId, int month, String dealerId);

    List<OemFsMappingSnapshot> findAllSnapshotsInTimeRangeAndOemsAndGroupCodes(Set<String> groupCodes, Set<String> oemIds, Integer fromYear, Integer toYear, Integer fromMonth, Integer toMonth, String siteId);
    List<OemFsMappingSnapshot> findAllSnapshotsByYearVersionOemId(String oemId, Integer version, int year);

    void deleteSnapshotByYearAndMonth(String fsId, int month, String dealerId);

    void deleteSnapshots(String fsId, List<Integer> months, String dealerId);

    OemFsMappingSnapshot findOneSnapshot(String fsId, List<Integer> months, String dealerId);
        // get snapshots for given year for months {1, 2, 3, ...., untilMonth}
    List<OemFsMappingSnapshot> findAllSnapshotsUntilMonth(String fsId, int untilMonth, Collection<String> groupCodes, String dealerId);

    void updateDefaultSiteId(String dealerId);

    void updateFsIdInOemFsMappingSnapshots(FSEntry fsEntry);
    List<BulkWriteUpsert> updateSiteIdInBulk(Collection<OemFsMappingSnapshot> oemFsMappingSnapshots , String dealerId);
}