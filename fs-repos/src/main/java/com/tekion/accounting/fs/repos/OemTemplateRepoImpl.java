package com.tekion.accounting.fs.repos;

import com.google.common.collect.Sets;
import com.mongodb.BasicDBObject;
import com.mongodb.bulk.BulkWriteUpsert;
import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.beans.common.OemTemplate;
import com.tekion.accounting.fs.common.utils.TMongoUtils;
import com.tekion.core.mongo.BaseGlobalMongoRepository;
import com.tekion.core.serverconfig.beans.ModuleName;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.TStringUtils;
import com.tekion.core.utils.UserContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.tekion.accounting.fs.common.TConstants.*;


@Slf4j
@Component
public class OemTemplateRepoImpl extends BaseGlobalMongoRepository<OemTemplate> implements OemTemplateRepo {

    public OemTemplateRepoImpl() {
        super(ModuleName.UNIVERSAL_GLOBAL.name(), OemTemplate.class);
    }

    @Override
    public List<OemTemplate> findActiveTemplateByOemYearAndCountry(String oemId, Integer year, String country) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(TConstants.YEAR).is(year);
        criteria.and(OemTemplate.OEM_ID).is(oemId);
        criteria.and(OemTemplate.ACTIVE).is(true);
        criteria.and(COUNTRY).is(country);
        return this.getMongoTemplate().find(Query.query(criteria), OemTemplate.class);
    }

    @Override
    public List<BulkWriteUpsert> updateBulk(List<OemTemplate> oemTemplates) {
        if(TCollectionUtils.isEmpty(oemTemplates)){
            return null;
        }
        BulkOperations bulkOperations = getMongoTemplate().bulkOps(BulkOperations.BulkMode.ORDERED, OemTemplate.class);
        oemTemplates.forEach(oemTemplate -> {
            Query query = Query.query(Criteria.where(TConstants.ID).in(oemTemplate.getId()));
            if (TStringUtils.isBlank(oemTemplate.getId())) {
                query = Query.query(Criteria.where(TConstants.ID).in(new ObjectId().toHexString()));
            }
            BasicDBObject dbDoc = new BasicDBObject();
            getMongoTemplate().getConverter().write(oemTemplate, dbDoc);
            Update update = TMongoUtils.fromDBObjectExcludeNullFields(dbDoc, Sets.newHashSet("_id", "id"));
            bulkOperations.upsert(query, update);
        });
        return bulkOperations.execute().getUpserts();
    }

    @Override
    public void updateTemplatesAsInactive(String oemId, Integer year, String country) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(OemTemplate.OEM_ID).is(oemId);
        criteria.and(OemTemplate.YEAR).is(year);
        criteria.and(COUNTRY).is(country);
        criteria.and(OemTemplate.ACTIVE).is(true);
        Query q = new Query(criteria);
        Update update = new Update();
        update.set(OemTemplate.ACTIVE, false);
        update.set(MODIFIED_BY_USER_ID, UserContextProvider.getCurrentUserId());
        update.set(MODIFIED_TIME, System.currentTimeMillis());
        getMongoTemplate().updateMulti(q, update, OemTemplate.class);
    }

    @Override
    public void addCountryInOemTemplate() {
        Criteria criteria = criteriaForNonDeleted();
        Query q = new Query(criteria);
        Update update = new Update();
        update.set(COUNTRY, COUNTRY_US);
        update.set(MODIFIED_TIME, System.currentTimeMillis());
        update.set(MODIFIED_BY_USER_ID, UserContextProvider.getCurrentUserId());
        getMongoTemplate().updateMulti(q, update, OemTemplate.class);
    }
}
