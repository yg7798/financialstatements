package com.tekion.accounting.fs.repos;

import com.mongodb.bulk.BulkWriteUpsert;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.beans.common.OEMFsCellCodeSnapshot;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface OEMFsCellCodeSnapshotRepo {
    void saveBulkSnapshot(List<OEMFsCellCodeSnapshot> oemExecutiveSnapshotList);

    List<OEMFsCellCodeSnapshot> findSnapshotByCodesAndMonth(String fsId, Integer month, Set<String> codes, String dealerId);

    List<OEMFsCellCodeSnapshot> findAllSnapshotByYearAndMonth(String oemId, Integer version, int year, int month, String dealerId, String siteId);

    List<OEMFsCellCodeSnapshot> findAllSnapshotByYearOemIdVersion(String oemId,int year, Integer version, String dealerId);

    List<OEMFsCellCodeSnapshot> findAllSnapshotByFsIdAndMonth(String fsId, int month, String dealerId);

    OEMFsCellCodeSnapshot findOneSnapshotByFsIdAndMonth(String fsId, int month, String dealerId);

    void deleteSnapshotByFsIdAndMonth(String fsId, int month, String dealerId);

    void hardDeleteSnapshotsInBulk(Collection<String> fsIds, List<Integer> months, String dealerId);

    void hardDeleteSnapshotByFsIdAndMonth(Collection<String> fsIds, String dealerId);

    void deleteBulkSnapshotByYearAndMonth(String oemId, int year, int fromMonth, int toMonth, String dealerId, String siteId );

    void updateDefaultSiteId(String dealerId);
    List<BulkWriteUpsert> updateSiteIdInBulk(Collection<OEMFsCellCodeSnapshot> oemFsCellCodeSnapshots, String dealerId);
    void updateFsIdInOemFsCellCodeSnapshots(FSEntry fsEntry);

    void updateFsTypeInFsCellCodeSnapshots(FSEntry fsEntry);
    List<OEMFsCellCodeSnapshot> findSnapshotByCodes(String fsId, Set<String> codes, String dealerId);

    List<OEMFsCellCodeSnapshot> getFsCellCodeByTimestamp(long fromTimestamp, long toTimestamp, Set<String> codes,
                                                         String oemId, String dealerId, String siteId);
    Integer addTenantId();

    /*
     this method hard deletes all snapshots created on or after given month and year for a dealer
    */
    void hardDeleteSnapshotByFsIdAndMonth(List<String> fsEntryList, Integer fromMonth, Integer fromYear, String currentDealerId);
}
