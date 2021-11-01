package com.tekion.accounting.fs.repos.worksheet;

import com.mongodb.bulk.BulkWriteUpsert;
import com.tekion.accounting.fs.beans.FSEntry;
import com.tekion.accounting.fs.beans.memo.HCWorksheet;

import java.util.Collection;
import java.util.List;

public interface HCWorksheetRepo {
    List<HCWorksheet> findForOemByYear(String oemId, int year, int version, String dealerId, String siteId);
    HCWorksheet findById(String id);
    List<HCWorksheet> findByIds(Collection<String> ids, String dealerId, String siteId);
    List<HCWorksheet> findByFsId(String fsId);
    void insertBulk(List<HCWorksheet> hcWorksheets);
    List<BulkWriteUpsert> updateBulk(Collection<HCWorksheet> hcWorksheets, String dealerId);
    HCWorksheet save(HCWorksheet hcWorksheet);
    void updateDefaultSiteId(String dealerId);
    void updateFsIdInHCWorksheets(FSEntry fsEntry);
    List<HCWorksheet> findByOemIdYearVersion(String oemId, int year, int version);
}
