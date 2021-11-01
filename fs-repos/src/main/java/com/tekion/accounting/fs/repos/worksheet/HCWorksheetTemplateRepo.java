package com.tekion.accounting.fs.repos.worksheet;

import com.mongodb.bulk.BulkWriteUpsert;
import com.tekion.accounting.fs.beans.memo.HCWorksheetTemplate;

import java.util.List;

public interface HCWorksheetTemplateRepo {
    HCWorksheetTemplate findById(String id);
    HCWorksheetTemplate findForOemByYearAndCountry(String oemId, int year, int version, String country);
    HCWorksheetTemplate save(HCWorksheetTemplate hcWorksheetTemplate);
    List<BulkWriteUpsert> upsertBulk(List<HCWorksheetTemplate> hcWorksheetTemplateList);
    void addCountryInHeadCountWorksheetTemplate();
}
