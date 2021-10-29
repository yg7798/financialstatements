package com.tekion.accounting.fs.repos;

import com.mongodb.bulk.BulkWriteUpsert;
import com.tekion.accounting.fs.beans.OemTemplate;

import java.util.List;

public interface OemTemplateRepo {
    void insertBulk(List<OemTemplate> oemTemplateList);

    List<BulkWriteUpsert> updateBulk(List<OemTemplate> oemTemplates);

    List<OemTemplate> findActiveTemplateByOemYearAndCountry(String oemId, Integer year, String country);

    void updateTemplatesAsInactive(String oemId, Integer year, String country);

    void addCountryInOemTemplate();
}
