package com.tekion.accounting.fs.repos;


import com.tekion.accounting.fs.master.beans.OEMFinancialMappingMedia;

public interface OEMFinancialMappingMediaRepository{
    OEMFinancialMappingMedia saveMedia(OEMFinancialMappingMedia media);
    OEMFinancialMappingMedia findSavedMediaByDealerIdNonDeleted(String oem, String year, String dealerId);
}
