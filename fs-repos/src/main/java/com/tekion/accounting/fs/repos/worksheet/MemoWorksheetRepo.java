package com.tekion.accounting.fs.repos.worksheet;

import com.mongodb.bulk.BulkWriteUpsert;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.beans.memo.MemoWorksheet;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface MemoWorksheetRepo {
    MemoWorksheet findById(String id);
    List<MemoWorksheet> findByFSId(String fsId);
    List<MemoWorksheet> findForOemByYearOptimized(String fsId, String dealerId);
    List<MemoWorksheet> findForOemByYearsOptimized(String fsId, String dealerId);
    List<MemoWorksheet> findByKeys(String fsId, Collection<String> keys, String dealerId);
    List<MemoWorksheet> findByIds(Collection<String> ids, String dealerId);
    MemoWorksheet save(MemoWorksheet memoWorkSheetTemplate);
    void insertBulk(List<MemoWorksheet> beans);
    void deleteWorkSheetsByFsId(String fsId, String dealerId);
    List<BulkWriteUpsert> updateBulk(List<MemoWorksheet> memoWorksheets,String dealerId);
    /* TODO - write global migration api for this */
    void deleteMemoWorksheetsByKeys(String fsId, Set<String> keys, String dealerId);
    void updateDefaultSiteId(String dealerId);
    void updateFsIdInMemoWorksheet(FSEntry fsEntry);
    List<MemoWorksheet> findForOemByYearOemIdVersion(String oemId, int year, int version);
    Integer addTenantId();
}
