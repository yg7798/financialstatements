package com.tekion.accounting.fs.repos.worksheet;

import com.google.common.collect.Sets;
import com.mongodb.BasicDBObject;
import com.mongodb.bulk.BulkWriteUpsert;
import com.tekion.accounting.fs.beans.memo.MemoWorksheetTemplate;
import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.beans.common.OemTemplate;
import com.tekion.accounting.fs.beans.memo.HCWorksheetTemplate;
import com.tekion.accounting.fs.common.utils.TMongoUtils;
import com.tekion.core.mongo.BaseGlobalMongoRepository;
import com.tekion.core.serverconfig.beans.ModuleName;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.TStringUtils;
import com.tekion.core.utils.UserContextProvider;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static com.tekion.accounting.fs.common.TConstants.*;

@Component
public class HCWorksheetTemplateRepoImpl extends BaseGlobalMongoRepository<HCWorksheetTemplate> implements HCWorksheetTemplateRepo {

    public HCWorksheetTemplateRepoImpl() {
        super(ModuleName.UNIVERSAL_GLOBAL.name(), HCWorksheetTemplate.class);
    }

    @Override
    public HCWorksheetTemplate findForOemByYearAndCountry(String oemId, int year, int version, String country) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(OemTemplate.OEM_ID).is(oemId);
        criteria.and(TConstants.YEAR).is(year);
        criteria.and(TConstants.VERSION).is(version);
        criteria.and(COUNTRY).is(country);
        return this.getMongoTemplate().findOne(Query.query(criteria),HCWorksheetTemplate.class);
    }

    @Override
    public List<BulkWriteUpsert> upsertBulk(List<HCWorksheetTemplate> hcWorksheetTemplateList) {
        if (TCollectionUtils.isEmpty(hcWorksheetTemplateList)) {
            return null;
        }
        BulkOperations bulkOperations = getMongoTemplate().bulkOps(BulkOperations.BulkMode.ORDERED, HCWorksheetTemplate.class);
        hcWorksheetTemplateList.forEach(hcWorksheetTemplate -> {
            Query query = Query.query(
                    Criteria.where(OemTemplate.OEM_ID).is(hcWorksheetTemplate.getOemId()).
                            and(TConstants.YEAR).is(hcWorksheetTemplate.getYear()).
                            and(TConstants.VERSION).is(hcWorksheetTemplate.getVersion()).
                            and(COUNTRY).is(hcWorksheetTemplate.getCountry())
            );
            BasicDBObject dbDoc = new BasicDBObject();
            getMongoTemplate().getConverter().write(hcWorksheetTemplate, dbDoc);
            Update update = TMongoUtils.fromDBObjectExcludeNullFields(dbDoc, Sets.newHashSet("_id", "id"));
            bulkOperations.upsert(query, update);
        });
        return bulkOperations.execute().getUpserts();
    }

    @Override
    public void addCountryInHeadCountWorksheetTemplate() {
        Criteria criteria = criteriaForNonDeleted();
        Query q = new Query(criteria);
        Update update = new Update();
        update.set(COUNTRY, COUNTRY_US);
        update.set(MODIFIED_TIME, System.currentTimeMillis());
        update.set(MODIFIED_BY_USER_ID, UserContextProvider.getCurrentUserId());
        getMongoTemplate().updateMulti(q, update, HCWorksheetTemplate.class);
    }

    @Override
    public List<HCWorksheetTemplate> findBySortByIdAndPageToken(String nextPageToken, int batchSize) {
        Criteria criteria = criteriaForNonDeleted();
        Query query = new Query(criteria);
        Sort sortByIds = new Sort(Sort.DEFAULT_DIRECTION, ID);
        if(TStringUtils.isNotBlank(nextPageToken)){
            criteria.is(ID).gt(nextPageToken);
        }
        return this.getMongoTemplate().find(query.limit(batchSize).with(sortByIds), HCWorksheetTemplate.class);
    }

    @Override
    public List<HCWorksheetTemplate> findByIds(Set<String> hcTemplateIds) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(ID).in(hcTemplateIds);
        return getMongoTemplate().find(Query.query(criteria), HCWorksheetTemplate.class);
    }
}
