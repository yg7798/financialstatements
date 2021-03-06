package com.tekion.accounting.fs.repos;

import com.mongodb.bulk.BulkWriteUpsert;
import com.tekion.accounting.fs.beans.common.OemTemplate;

import java.util.List;

public interface OemTemplateRepo {
    void insertBulk(List<OemTemplate> oemTemplateList);

    List<BulkWriteUpsert> updateBulk(List<OemTemplate> oemTemplates);

    List<OemTemplate> findActiveTemplateByOemYearAndCountry(String oemId, Integer year, String country, String locale);

    void updateTemplatesAsInactive(String oemId, Integer year, String country, String locale);

    void addCountryInOemTemplate();

    List<OemTemplate> findAllOemDetails();

    void migrateLocale(String country, String locale);
}
