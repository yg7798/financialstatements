package com.tekion.accounting.fs.repos;

import com.mongodb.bulk.BulkWriteResult;
import com.tekion.accounting.fs.beans.mappings.OEMFinancialMapping;
import com.tekion.accounting.fs.beans.mappings.OemFsMapping;

import java.util.List;

public interface OEMFinancialMappingRepository {
    List<OEMFinancialMapping> findMappingsForGLAccount(String accountId, String glAccountId);

    BulkWriteResult deleteMappings(List<OEMFinancialMapping> mappings);
    BulkWriteResult upsertMappings(List<OEMFinancialMapping> mappings);
    List<OEMFinancialMapping> findMappingsByFsIdAndDealerId(String fsId, String dealerId);

    OemFsMapping save(OemFsMapping oemFsMapping);

    void updateDefaultSiteId(String dealerId);
}
