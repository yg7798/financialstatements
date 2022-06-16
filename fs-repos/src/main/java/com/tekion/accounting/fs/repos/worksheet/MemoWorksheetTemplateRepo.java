package com.tekion.accounting.fs.repos.worksheet;

import com.mongodb.bulk.BulkWriteUpsert;
import com.tekion.accounting.fs.beans.memo.MemoWorksheetTemplate;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.multilingual.dto.TekMultiLingualBean;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MemoWorksheetTemplateRepo {
    MemoWorksheetTemplate findById(String id);
    List<MemoWorksheetTemplate> findByOemYearAndCountry(String oemId, int year, int version, String country);
    MemoWorksheetTemplate save(MemoWorksheetTemplate memoWorkSheetTemplate);
    void insertBulk(List<MemoWorksheetTemplate> beans);
    List<BulkWriteUpsert> updateBulk(List<MemoWorksheetTemplate> memoWorksheetTemplates);
    List<MemoWorksheetTemplate> findByOemYearAndCountry(String oemId, int year, int version, Collection<String> keys, String country);
    void deleteTemplatesByKey(String oemId, int year, int version, Set<String> keys, String country);
    void addCountryInMemoWorksheetTemplate();

    List<MemoWorksheetTemplate> findBySortByIdAndPageToken(String nextPageToken, int batchSize);

    void languagesBulkUpdate(Map<String, TekMultiLingualBean> keyToValueMap);

    void deleteMWTemplatesByOemByCountryByYear(OEM oemId, Integer year, String countryCode);

    void deleteMWTemplatesByOemByCountryByYearByKeys(OEM oemId, Integer year, Set<String> keys, String countryCode);
}
