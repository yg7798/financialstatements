package com.tekion.accounting.fs.repos;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mongodb.BasicDBObject;
import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.enums.AccountingError;
import com.tekion.accounting.fs.enums.FSType;
import com.tekion.accounting.fs.common.utils.TMongoUtils;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.mongo.BaseDealerLevelMongoRepository;
import com.tekion.core.utils.UserContextProvider;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static com.tekion.accounting.fs.beans.common.FSEntry.OEM_ID;
import static com.tekion.accounting.fs.common.TConstants.*;
import static com.tekion.accounting.fs.beans.common.FSEntry.FS_TYPE;
import static com.tekion.accounting.fs.common.utils.UserContextUtils.getDefaultSiteId;

@Component
public class FSEntryRepoImpl extends BaseDealerLevelMongoRepository<FSEntry> implements FSEntryRepo {

    public FSEntryRepoImpl()   {
        super(TENANT_DEFAULT, FSEntry.class);
    }

    @Override
    public List<FSEntry> findByKey(String key, Object value, String dealerId, String siteId) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(DEALER_ID).is(dealerId);
        criteria.and(key).is(value);
        criteria.and(SITE_ID).is(siteId);
        return this.getMongoTemplate().find(Query.query(criteria), FSEntry.class);
    }

    @Override
    public List<FSEntry> findByKeyAtDealerLevel(String key, Object value, String dealerId) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(DEALER_ID).is(dealerId);
        criteria.and(key).is(value);
        return this.getMongoTemplate().find(Query.query(criteria), FSEntry.class);
    }

    @Override
    public void updateDefaultSiteId(String dealerId) {
        Criteria criteria = criteriaForNonDeleted().and(DEALER_ID).is(dealerId);
        Update update = new Update();
        update.set(TConstants.SITE_ID, getDefaultSiteId());
        update.set(TConstants.MODIFIED_TIME,System.currentTimeMillis());
        update.set(TConstants.MODIFIED_BY_USER_ID, UserContextProvider.getCurrentUserId());
        getMongoTemplate().updateMulti(Query.query(criteria), update, FSEntry.class);
    }

    @Override
    public List<FSEntry> fetchAllByDealerIdNonDeleted(String dealerId) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(DEALER_ID).is(dealerId);
        Sort sort = Sort.by(Sort.Direction.DESC, YEAR)
                .and(new Sort(Sort.Direction.DESC, FSEntry.VERSION))
                .and(new Sort(Sort.Direction.DESC, FSEntry.OEM_ID));
        return this.getMongoTemplate().find(Query.query(criteria).with(sort), FSEntry.class);
    }

    @Override
    public List<FSEntry> findFsEntriesForYear(Integer year, String dealerId, String siteId) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(TConstants.YEAR).is(year);
        criteria.and(TConstants.DEALER_ID).is(dealerId);
        criteria.and(SITE_ID).is(siteId);
        Sort sort = Sort.by(Sort.Direction.DESC, TConstants.VERSION);
        return this.getMongoTemplate().find(Query.query(criteria).with(sort), FSEntry.class);
    }

    @Override
    public FSEntry findByOem(String oemId, int version, String dealerId){
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(TConstants.DEALER_ID).is(dealerId);
        Sort sort = Sort.by(Sort.Direction.DESC, YEAR);
        return this.getMongoTemplate().findOne(Query.query(criteria).with(sort), FSEntry.class);
    }

    @Override
    public List<FSEntry> fetchAllByDealerIdAndSiteId(String dealerId, String siteId) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(DEALER_ID).is(dealerId);
        criteria.and(SITE_ID).is(siteId);
        Sort sort = Sort.by(Sort.Direction.DESC, YEAR)
                .and(new Sort(Sort.Direction.DESC, FSEntry.VERSION))
                .and(new Sort(Sort.Direction.DESC, FSEntry.OEM_ID));
        return this.getMongoTemplate().find(Query.query(criteria).with(sort), FSEntry.class);
    }

    @Override
    public FSEntry findByIdAndDealerId(String id, String dealerId) {
        Criteria criteria = this.criteriaForNonDeleted();
        criteria.and(ID).is(id);
        criteria.and(TConstants.DEALER_ID).is(dealerId);
        return this.findOne(criteria, this.getBeanClass(), this.getMongoTemplate());
    }

    @Override
    public FSEntry findByIdAndDealerIdWithNullCheck(String id, String dealerId) {
        FSEntry fsEntry = findByIdAndDealerId(id, dealerId);
        if(Objects.isNull(fsEntry)){
            throw new TBaseRuntimeException(AccountingError.fsEntryNotFoundById);
        }
        return fsEntry;
    }

    @Override
    public List<FSEntry> find(String oemId, int year, String dealerId, String siteId, String fsType) {
        Criteria criteria = getCriteriaForOemYearAndFSType(oemId, year, dealerId, siteId, fsType);
        return this.getMongoTemplate().find(Query.query(criteria), FSEntry.class);
    }

    @Override
    public FSEntry findDefaultType(String oemId, int year, String dealerId, String siteId) {
        Criteria criteria = getCriteriaForOemYearAndFSType(oemId, year, dealerId, siteId, FSType.OEM.name());
        FSEntry fsEntry = this.getMongoTemplate().findOne(Query.query(criteria), FSEntry.class);
        if(Objects.isNull(fsEntry)){
            throw new TBaseRuntimeException(AccountingError.fsEntryNotFoundForRequestedOemAndYear, oemId, String.valueOf(year));
        }
        return fsEntry;
    }

    @Override
    public FSEntry findDefaultTypeWithoutNullCheck(String oemId, int year, String dealerId, String siteId) {
        Criteria criteria = getCriteriaForOemYearAndFSType(oemId, year, dealerId, siteId, FSType.OEM.name());
        return this.getMongoTemplate().findOne(Query.query(criteria), FSEntry.class);
    }

    private Criteria getCriteriaForOemYearAndFSType(String oemId, int year, String dealerId, String siteId, String fsType){
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(DEALER_ID).is(dealerId);
        criteria.and(FSEntry.OEM_ID).is(oemId);
        criteria.and(FSEntry.YEAR).is(year);
        criteria.and(SITE_ID).is(siteId);
        criteria.and(FS_TYPE).is(fsType);
        return criteria;
    }

    @Override
    public List<FSEntry> getFSEntries(String dealerId) {
        Criteria criteria = this.criteriaForNonDeleted();
        criteria.and(TConstants.DEALER_ID).is(dealerId);
        Sort sort = Sort.by(Sort.Direction.ASC, FSEntry.OEM_ID);
        return this.getMongoTemplate().find(Query.query(criteria).with(sort), FSEntry.class);
    }

    @Override
    public List<FSEntry> getAllFSEntriesByFsType(String fsType, String dealerId) {
        Criteria criteria = this.criteriaForNonDeleted();
        criteria.and(TConstants.DEALER_ID).is(dealerId);
        criteria.and(FS_TYPE).is(fsType);
        return this.findAll(criteria, this.getBeanClass(), this.getMongoTemplate());
    }


    @Override
    public List<FSEntry> findFSEntriesByOem(String oemId, String dealerId) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(DEALER_ID).is(dealerId);
        criteria.and(FSEntry.OEM_ID).is(oemId);
        return this.getMongoTemplate().find(Query.query(criteria), FSEntry.class);
    }

    @Override
    public void bulkUpsert(List<FSEntry> fsEntries){
        if(fsEntries.size() == 0) {
            return;
        }
        List<Pair<Query, Update>> updates = Lists.newArrayList();
        BulkOperations bulkOperations = getMongoTemplate().bulkOps(BulkOperations.BulkMode.ORDERED, FSEntry.class);
        (fsEntries).stream().filter(Objects::nonNull).forEach(fsEntry -> {
            Query query = Query.query(Criteria.where(ID).is(fsEntry.getId()));
            BasicDBObject dbDoc = new BasicDBObject();
            getMongoTemplate().getConverter().write(fsEntry, dbDoc);
            Update update = TMongoUtils.fromDBObjectExcludeNullFields(dbDoc, Sets.newHashSet("_id", "_class"));
            updates.add(Pair.of(query, update));
        });
        bulkOperations.upsert(updates);
        bulkOperations.execute();
    }

    @Override
    public FSEntry findByOemYearVersion(String oemId, int year, int version, String dealerId) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(DEALER_ID).is(dealerId);
        criteria.and(FSEntry.OEM_ID).is(oemId);
        criteria.and(FSEntry.YEAR).is(year);
        criteria.and(FSEntry.VERSION).is(version);
        return this.getMongoTemplate().findOne(Query.query(criteria), FSEntry.class);
    }

    @Override
    public List<FSEntry> findByOemYearVersionAndSite(String oemId, Integer year, Integer version, String dealerId, String siteId){
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(DEALER_ID).is(dealerId);
        if(Objects.nonNull(siteId)){
            criteria.and(SITE_ID).is(siteId);
        }
        criteria.and(FSEntry.OEM_ID).is(oemId);
        criteria.and(FSEntry.YEAR).is(year);
        criteria.and(FSEntry.VERSION).is(version);
        return this.getMongoTemplate().find(Query.query(criteria), FSEntry.class);
    }

    @Override
    public List<FSEntry> findFsEntriesByYearRange(String oemId, Integer fromYear, Integer toYear, String fsType, String dealerId, String siteId) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(DEALER_ID).is(dealerId);
        criteria.and(SITE_ID).is(siteId);
        criteria.and(FSEntry.OEM_ID).is(oemId);
        criteria.and(FSEntry.YEAR).gte(fromYear).lte(toYear);
        criteria.and(FS_TYPE).is(fsType);
        return this.findAll(criteria, this.getBeanClass(), this.getMongoTemplate());
    }

    @Override
    public List<FSEntry> findFsEntriesForDealer(Integer year, String dealerId){
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(DEALER_ID).is(dealerId);
        criteria.and(FSEntry.YEAR).is(year);
        return this.findAll(criteria, this.getBeanClass(), this.getMongoTemplate());
    }

    @Override
    public List<FSEntry> findByOemFsTypeDealerIdAndSiteId(String oemId, String fsType, String dealerId, String siteId) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(DEALER_ID).is(dealerId);
        criteria.and(SITE_ID).is(siteId);
        criteria.and(FSEntry.OEM_ID).is(oemId);
        criteria.and(FS_TYPE).is(fsType);
        return this.findAll(criteria, this.getBeanClass(), this.getMongoTemplate());
    }

    @Override
    public List<FSEntry> getFSEntriesBySiteId(String dealerId, List<String> siteIds){
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(DEALER_ID).is(dealerId);
        criteria.and(SITE_ID).in(siteIds);
        return this.findAll(criteria, this.getBeanClass(), this.getMongoTemplate());
    }

    @Override
    public List<FSEntry> getFsEntriesByOemIds(FSType fsType, List<String> oemIds, Integer year, String dealerId) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(DEALER_ID).is(dealerId);
        criteria.and(YEAR).is(year);
        criteria.and(FS_TYPE).is(fsType);
        criteria.and(OEM_ID).in(oemIds);
        return this.findAll(criteria, this.getBeanClass(), this.getMongoTemplate());
    }

}
