package com.tekion.accounting.fs.repos.worksheet;

import com.google.common.collect.Sets;
import com.mongodb.BasicDBObject;
import com.mongodb.bulk.BulkWriteUpsert;
import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.beans.common.OemTemplate;
import com.tekion.accounting.fs.beans.memo.MemoWorksheetTemplate;
import com.tekion.accounting.fs.common.utils.TMongoUtils;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.core.mongo.BaseGlobalMongoRepository;
import com.tekion.core.serverconfig.beans.ModuleName;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.TStringUtils;
import com.tekion.core.utils.UserContextProvider;
import com.tekion.multilingual.dto.TekMultiLingualBean;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.tekion.accounting.fs.common.TConstants.*;

@Component
public class MemoWorkSheetTemplateRepoImpl extends BaseGlobalMongoRepository<MemoWorksheetTemplate> implements MemoWorksheetTemplateRepo {

    public MemoWorkSheetTemplateRepoImpl() {
        super(ModuleName.UNIVERSAL_GLOBAL.name(), MemoWorksheetTemplate.class);
    }

    @Override
    public List<MemoWorksheetTemplate> findByOemYearAndCountry(String oemId, int year, int version, String country) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(OemTemplate.OEM_ID).is(oemId);
        criteria.and(TConstants.YEAR).is(year);
        criteria.and(TConstants.VERSION).is(version);
        criteria.and(COUNTRY).is(country);
        return this.getMongoTemplate().find(Query.query(criteria), MemoWorksheetTemplate.class);
    }

    @Override
    public List<BulkWriteUpsert> updateBulk(List<MemoWorksheetTemplate> memoWorksheetTemplates) {
        if(TCollectionUtils.isEmpty(memoWorksheetTemplates)){
            return null;
        }
        BulkOperations bulkOperations = getMongoTemplate().bulkOps(BulkOperations.BulkMode.ORDERED, MemoWorksheetTemplate.class);
        memoWorksheetTemplates.forEach(memoWorksheetTemplate -> {
            Query query = Query.query(Criteria.where(TConstants.ID).in(memoWorksheetTemplate.getId()));
            if (TStringUtils.isBlank(memoWorksheetTemplate.getId())) {
                query = Query.query(Criteria.where(TConstants.ID).in(new ObjectId().toHexString()));
            }
            BasicDBObject dbDoc = new BasicDBObject();
            getMongoTemplate().getConverter().write(memoWorksheetTemplate, dbDoc);
            Update update = TMongoUtils.fromDBObjectExcludeNullFields(dbDoc, Sets.newHashSet("_id", "id"));
            bulkOperations.upsert(query, update);
        });
        return bulkOperations.execute().getUpserts();
    }

    @Override
    public List<MemoWorksheetTemplate> findByOemYearAndCountry(String oemId, int year, int version, Collection<String> keys, String country) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(OemTemplate.OEM_ID).is(oemId);
        criteria.and(TConstants.YEAR).is(year);
        criteria.and(TConstants.VERSION).is(version);
        criteria.and(COUNTRY).is(country);
        criteria.and(TConstants.KEY).in(keys);
        return this.getMongoTemplate().find(Query.query(criteria), MemoWorksheetTemplate.class);
    }

    @Override
    public void deleteTemplatesByKey(String oemId, int year, int version, Set<String> keys, String country) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(OemTemplate.OEM_ID).is(oemId);
        criteria.and(TConstants.YEAR).is(year);
        criteria.and(TConstants.VERSION).is(version);
        criteria.and(TConstants.KEY).in(keys);
        criteria.and(COUNTRY).is(country);

        Update update = TMongoUtils.updateForMarkAsDeleted();
        update.set(OemTemplate.OEM_ID, oemId+"_"+System.currentTimeMillis());
        update.set(MODIFIED_TIME,System.currentTimeMillis());
        update.set(TConstants.MODIFIED_BY_USER_ID, UserContextProvider.getCurrentUserId());
        getMongoTemplate().updateMulti(Query.query(criteria), update, MemoWorksheetTemplate.class);
    }

    @Override
    public void addCountryInMemoWorksheetTemplate() {
        Criteria criteria = criteriaForNonDeleted();
        Query q = new Query(criteria);
        Update update = new Update();
        update.set(COUNTRY, COUNTRY_US);
        update.set(MODIFIED_TIME, System.currentTimeMillis());
        update.set(MODIFIED_BY_USER_ID, UserContextProvider.getCurrentUserId());
        getMongoTemplate().updateMulti(q, update, MemoWorksheetTemplate.class);
    }

    @Override
    public List<MemoWorksheetTemplate> findBySortByIdAndPageToken(String nextPageToken, int batchSize) {
        Criteria criteria = criteriaForNonDeleted();
        Query query = new Query(criteria);
        Sort sortByIds = new Sort(Sort.DEFAULT_DIRECTION, ID);
        if(TStringUtils.isNotBlank(nextPageToken)){
            criteria.is(ID).gt(nextPageToken);
        }
        return this.getMongoTemplate().find(query.limit(batchSize).with(sortByIds), MemoWorksheetTemplate.class);
    }

    @Override
    public void languagesBulkUpdate(Map<String, TekMultiLingualBean> keyToValueMap) {
        BulkOperations bulkOperations = getMongoTemplate().bulkOps(BulkOperations.BulkMode.UNORDERED, MemoWorksheetTemplate.class);
        keyToValueMap.forEach((id,languages) -> {
            Criteria criteria = criteriaForNonDeleted().and(ID).is(id);
            Update update = new Update();
            update.set(MemoWorksheetTemplate.LANGUAGES, languages);
            bulkOperations.updateOne(Query.query(criteria), update);
        });
        bulkOperations.execute();
    }

    @Override
    public void deleteMWTemplatesByOemByCountryByYear(OEM oemId, Integer year, String countryCode) {
        Criteria criteria = criteriaForNonDeleted();
        Query query = new Query(criteria);
        criteria.and(OemTemplate.OEM_ID).is(oemId);
        criteria.and(TConstants.YEAR).is(year);
        criteria.and(COUNTRY).is(countryCode);
        this.getMongoTemplate().remove(query, MemoWorksheetTemplate.class);
    }

    @Override
    public void deleteMWTemplatesByOemByCountryByYearByKeys(OEM oemId, Integer year, Set<String> keys, String countryCode) {
        Criteria criteria = criteriaForNonDeleted();
        Query query = new Query(criteria);
        criteria.and(OemTemplate.OEM_ID).is(oemId);
        criteria.and(TConstants.YEAR).is(year);
        criteria.and(COUNTRY).is(countryCode);
        criteria.and(KEY).in(keys);
        this.getMongoTemplate().remove(query, MemoWorksheetTemplate.class);
    }
}
