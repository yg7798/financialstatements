package com.tekion.accounting.fs.repos;

import com.tekion.accounting.fs.beans.accountingInfo.AccountingInfo;
import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.beans.mappings.OEMFinancialMapping;
import com.tekion.accounting.fs.beans.mappings.OEMFinancialMappingMedia;
import com.tekion.accounting.fs.common.utils.TMongoUtils;
import com.tekion.core.mongo.BaseDealerLevelMongoRepository;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import static com.tekion.accounting.fs.common.TConstants.TENANT_DEFAULT;


@Component
public class OEMFinancialMappingMediaRepositoryImpl extends
        BaseDealerLevelMongoRepository<OEMFinancialMappingMedia> implements OEMFinancialMappingMediaRepository {

    public OEMFinancialMappingMediaRepositoryImpl() {
        super(TENANT_DEFAULT , OEMFinancialMappingMedia.class);
    }

    @Override
    public OEMFinancialMappingMedia saveMedia(OEMFinancialMappingMedia media) {
        return this.getMongoTemplate().save(media);
    }

    @Override
    public OEMFinancialMappingMedia findSavedMediaByDealerIdNonDeleted(String oem, String year, String dealerId) {
        Criteria criteria = Criteria
                .where(TConstants.DELETED).is(false)
                .and(TConstants.DEALER_ID).is(dealerId)
                .and(OEMFinancialMapping.OEM_ID).is(oem)
                .and(OEMFinancialMapping.YEAR).is(year);
        return this.getMongoTemplate().findOne(Query.query(criteria), OEMFinancialMappingMedia.class);
    }

    @Override
    public Integer addTenantId(){
        BulkOperations bulkOperations= TMongoUtils.addTenantIdInMongoBean(getMongoTemplate(), OEMFinancialMappingMedia.class);
        return bulkOperations.execute().getModifiedCount();
    }
}
