package com.tekion.accounting.fs.repos.worksheet;

import com.mongodb.bulk.BulkWriteUpsert;
import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.beans.common.OemTemplate;
import com.tekion.accounting.fs.beans.mappings.OemFsMapping;
import com.tekion.accounting.fs.beans.memo.MemoWorksheet;
import com.tekion.accounting.fs.common.utils.TMongoUtils;
import com.tekion.core.mongo.BaseDealerLevelMongoRepository;
import com.tekion.core.serverconfig.beans.ModuleName;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.UserContextProvider;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.tekion.accounting.fs.common.TConstants.DEALER_ID;
import static com.tekion.accounting.fs.common.TConstants.SITE_ID;
import static com.tekion.accounting.fs.beans.common.FSEntry.FS_ID;
import static com.tekion.accounting.fs.beans.common.OemTemplate.OEM_ID;
import static com.tekion.accounting.fs.beans.memo.MemoWorksheet.FIELD_TYPE;
import static com.tekion.accounting.fs.common.utils.UserContextUtils.getDefaultSiteId;

@Service
public class MemoWorksheetRepoImpl extends BaseDealerLevelMongoRepository<MemoWorksheet> implements MemoWorksheetRepo {

    public MemoWorksheetRepoImpl() {
        super(ModuleName.TENANT_DEFAULT.name(), MemoWorksheet.class);
    }

    @Override
    public List<MemoWorksheet> findByFSId(String fsId) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(FS_ID).is(fsId);
        return this.getMongoTemplate().find(Query.query(criteria),MemoWorksheet.class);
    }

    @Override
    public List<MemoWorksheet> findForOemByYearOptimized(String fsId, String dealerId) {
        return findForOemByYearsOptimized(fsId, dealerId);
    }

    @Override
    public List<MemoWorksheet> findForOemByYearsOptimized(String fsId, String dealerId) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(DEALER_ID).is(dealerId);
        criteria.and(FS_ID).is(fsId);
        Query query = Query.query(criteria);
        query.fields().include("key").include(MemoWorksheet.VALUES).include(TConstants.YEAR).include(FIELD_TYPE);
        return this.getMongoTemplate().find(query, MemoWorksheet.class);
    }

    @Override
    public List<MemoWorksheet> findByIds(Collection<String> ids, String dealerId) {
        if(TCollectionUtils.isEmpty(ids)){
            return Collections.emptyList();
        }
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(DEALER_ID).is(dealerId);
        criteria.and(TConstants.ID).in(ids);
        return this.getMongoTemplate().find(Query.query(criteria),MemoWorksheet.class);
    }

    @Override
    public void deleteWorkSheetsByFsId(String fsId, String dealerId) {
        Criteria criteria = new Criteria(DEALER_ID).is(dealerId);
        criteria.and(FS_ID).is(fsId);
        getMongoTemplate().findAllAndRemove(Query.query(criteria),MemoWorksheet.class);
    }

    @Override
    public void deleteMemoWorksheetsByKeys(String fsId, Set<String> keys, String dealerId) {
        Criteria criteria = getCriteriaToFetchWorksheetsByKey(fsId, keys, dealerId);
        Update update = TMongoUtils.updateForMarkAsDeleted();
        update.set(TConstants.MODIFIED_TIME,System.currentTimeMillis());
        update.set(TConstants.MODIFIED_BY_USER_ID, UserContextProvider.getCurrentUserId());
        getMongoTemplate().updateMulti(Query.query(criteria), update, MemoWorksheet.class);
    }

    private Criteria getCriteriaToFetchWorksheetsByKey(String fsId, Collection<String> keys, String dealerId) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(DEALER_ID).is(dealerId);
        if((StringUtils.isNotBlank(fsId))){
            criteria.and(FS_ID).is(fsId);
        }
        criteria.and(TConstants.KEY).in(keys);
        return criteria;
    }

    @Override
    public List<BulkWriteUpsert> updateBulk(List<MemoWorksheet> memoWorksheets,String dealerId) {
        if(TCollectionUtils.isEmpty(memoWorksheets)){
            return null;
        }
        BulkOperations bulkOperations = getMongoTemplate().bulkOps(BulkOperations.BulkMode.ORDERED, MemoWorksheet.class);
        memoWorksheets.forEach(memoWorksheet -> {
            Query query = Query.query(Criteria.where(TConstants.ID).in(memoWorksheet.getId()));
            Update update = new Update();
            update.set(MemoWorksheet.VALUES,memoWorksheet.getValues());
            update.set(MemoWorksheet.ACTIVE, memoWorksheet.getActive());
            update.set(TConstants.MODIFIED_TIME,System.currentTimeMillis());
            update.set(SITE_ID,memoWorksheet.getSiteId());
            update.set(TConstants.MODIFIED_BY_USER_ID, UserContextProvider.getCurrentUserId());
            bulkOperations.upsert(query,update);
        });
        return bulkOperations.execute().getUpserts();
    }


    @Override
    public List<MemoWorksheet> findByKeys(String fsId, Collection<String> keys, String dealerId) {
        if(TCollectionUtils.isEmpty(keys)){
            return Collections.emptyList();
        }
        Criteria criteria = getCriteriaToFetchWorksheetsByKey(fsId, keys, dealerId);
        return this.getMongoTemplate().find(Query.query(criteria),MemoWorksheet.class);
    }

    @Override
    public void updateDefaultSiteId(String dealerId) {
        Criteria criteria = criteriaForNonDeleted().and(DEALER_ID).is(dealerId);
        Update update = new Update();
        update.set(SITE_ID, getDefaultSiteId());
        update.set(TConstants.MODIFIED_TIME,System.currentTimeMillis());
        update.set(TConstants.MODIFIED_BY_USER_ID, UserContextProvider.getCurrentUserId());
        getMongoTemplate().updateMulti(Query.query(criteria), update, MemoWorksheet.class);
    }

    @Override
    public void updateFsIdInMemoWorksheet(FSEntry fsEntry) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(TConstants.DEALER_ID_KEY).is(fsEntry.getDealerId());
        criteria.and(TConstants.YEAR).is(fsEntry.getYear());
        criteria.and(SITE_ID).is(fsEntry.getSiteId());

        String oemId = fsEntry.getOemId();
        if(fsEntry.getFsType().equalsIgnoreCase("Internal")){
            oemId = oemId + "Internal";
        }
        criteria.and(OemFsMapping.OEM_ID).is(oemId);

        Update update = new Update();
        update.set(FS_ID, fsEntry.getId());
        update.set(OEM_ID, fsEntry.getOemId());
        update.set(TConstants.MODIFIED_TIME,System.currentTimeMillis());
        update.set(TConstants.MODIFIED_BY_USER_ID, UserContextProvider.getCurrentUserId());
        getMongoTemplate().updateMulti(Query.query(criteria), update, MemoWorksheet.class);
    }

    @Override
    public List<MemoWorksheet> findForOemByYearOemIdVersion(String oemId, int year, int version) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(DEALER_ID).is(UserContextProvider.getCurrentDealerId());
        criteria.and(OemTemplate.OEM_ID).is(oemId);
        criteria.and(TConstants.YEAR).is(year);
        criteria.and(TConstants.VERSION).is(version);
        return this.getMongoTemplate().find(Query.query(criteria),MemoWorksheet.class);
    }

    @Override
    public List<MemoWorksheet> findByFsIds(Collection<String> fsIds) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(FS_ID).in(fsIds);
        return this.getMongoTemplate().find(Query.query(criteria),MemoWorksheet.class);
    }

    @Override
    public Integer addTenantId(){
        BulkOperations bulkOperations= TMongoUtils.addTenantIdInMongoBean(getMongoTemplate(), MemoWorksheet.class);
        return bulkOperations.execute().getModifiedCount();
    }
}
