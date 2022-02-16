package com.tekion.accounting.fs.repos;


import com.tekion.accounting.fs.beans.mappings.OEMFinancialMappingMedia;

public interface OEMFinancialMappingMediaRepository{
    OEMFinancialMappingMedia saveMedia(OEMFinancialMappingMedia media);
    OEMFinancialMappingMedia findSavedMediaByDealerIdNonDeleted(String oem, String year, String dealerId);
    Integer addTenantId();
}
