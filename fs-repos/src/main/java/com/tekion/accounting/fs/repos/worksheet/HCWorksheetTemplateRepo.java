package com.tekion.accounting.fs.repos.worksheet;

import com.mongodb.bulk.BulkWriteUpsert;
import com.tekion.accounting.fs.beans.memo.HCWorksheetTemplate;

import java.util.List;
import java.util.Set;

public interface HCWorksheetTemplateRepo {
    HCWorksheetTemplate findById(String id);
    HCWorksheetTemplate findForOemByYearAndCountry(String oemId, int year, int version, String country);
    HCWorksheetTemplate save(HCWorksheetTemplate hcWorksheetTemplate);
    List<BulkWriteUpsert> upsertBulk(List<HCWorksheetTemplate> hcWorksheetTemplateList);
    void addCountryInHeadCountWorksheetTemplate();

    List<HCWorksheetTemplate> findBySortByIdAndPageToken(String nextPageToken, int batchSize);

    List<HCWorksheetTemplate> findByIds(Set<String> hcTemplateIds);
}
