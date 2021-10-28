package com.tekion.accounting.fs.repos;

import com.tekion.accounting.fs.TConstants;
import com.tekion.accounting.fs.master.beans.OEMFinancialMapping;
import com.tekion.accounting.fs.master.beans.OEMFinancialMappingMedia;
import com.tekion.core.mongo.BaseDealerLevelMongoRepository;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import static com.tekion.accounting.fs.TConstants.TENANT_DEFAULT;


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
}
