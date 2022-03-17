package com.tekion.accounting.fs.repos;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mongodb.BasicDBObject;
import com.mongodb.bulk.BulkWriteUpsert;
import com.mongodb.client.result.UpdateResult;
import com.tekion.accounting.fs.beans.accountingInfo.AccountingInfo;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.beans.mappings.OemFsMapping;
import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.common.utils.JsonUtil;
import com.tekion.accounting.fs.common.utils.TMongoUtils;
import com.tekion.accounting.fs.dto.mappings.OemFsGroupCodeDetails;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.core.beans.TBaseMongoBean;
import com.tekion.core.mongo.BaseDealerLevelMongoRepository;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.TStringUtils;
import com.tekion.core.utils.UserContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.tekion.accounting.fs.beans.common.FSEntry.FS_ID;
import static com.tekion.accounting.fs.beans.common.FSEntry.FS_TYPE;
import static com.tekion.accounting.fs.beans.common.OemFSMetadataCellsInfo.OEM_ID;
import static com.tekion.accounting.fs.common.TConstants.*;
import static com.tekion.accounting.fs.common.utils.UserContextUtils.getDefaultSiteId;

@Slf4j
@Component
public class OemFSMappingRepoImpl extends BaseDealerLevelMongoRepository<OemFsMapping> implements OemFSMappingRepo {

    public OemFSMappingRepoImpl() {
        super(TENANT_DEFAULT , OemFsMapping.class);
    }

    @Override
    public List<OemFsMapping> getMappings(String fsId, String dealerId) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(TConstants.DEALER_ID_KEY).is(dealerId);
        criteria.and(FS_ID).is(fsId);
        Query query = Query.query(criteria);
        //query.fields().include(OemFsMapping.FS_CELL_GROUP_CODE).include(OemFsMapping.GL_ACCT_ID).include(DEALER_ID);
        return this.getMongoTemplate().find(query, OemFsMapping.class);
    }

    @Override
    public List<OemFsMapping> getMappingsByGroupCodes(String dealerId, Integer year, Integer version, String oemId, Collection<String> groupCodes, String siteId) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(TConstants.DEALER_ID_KEY).is(dealerId);
        criteria.and(SITE_ID).is(siteId);
        criteria.and(TConstants.YEAR).is(year);
        criteria.and(OemFsMapping.VERSION).is(version);
        criteria.and(OemFsMapping.OEM_ID).is(oemId);
        criteria.and(OemFsMapping.FS_CELL_GROUP_CODE).in(groupCodes);
        Query query = Query.query(criteria);
        //query.fields().include(OemFsMapping.FS_CELL_GROUP_CODE).include(OemFsMapping.GL_ACCT_ID).include(DEALER_ID).include(SITE_ID);
        return this.getMongoTemplate().find(query, OemFsMapping.class);
    }

    @Override
    public List<BulkWriteUpsert> updateBulk(List<OemFsMapping> oemFsMappings) {
        if(TCollectionUtils.isEmpty(oemFsMappings)){
            return null;
        }
        BulkOperations bulkOperations = getMongoTemplate().bulkOps(BulkOperations.BulkMode.ORDERED, OemFsMapping.class);
        oemFsMappings.forEach(oemFsMapping -> {
            Query query = Query.query(Criteria.where(TConstants.ID).in(oemFsMapping.getId()));
            if (TStringUtils.isBlank(oemFsMapping.getId())) {
                query = Query.query(Criteria.where(TConstants.ID).in(new ObjectId().toHexString()));
            }
            BasicDBObject dbDoc = new BasicDBObject();
            getMongoTemplate().getConverter().write(oemFsMapping, dbDoc);
            Update update = TMongoUtils.fromDBObjectExcludeNullFields(dbDoc, Sets.newHashSet("_id", "id"));
            bulkOperations.upsert(query, update);
        });
        return bulkOperations.execute().getUpserts();
    }

    @Override
    public List<OemFsMapping> findByGlAccountIdAndYearIncludeDeleted(String fsId, Collection<String> glAccountIds, String dealerId) {

        Criteria criteria = new Criteria(TConstants.GLACCOUNT_ID).in(glAccountIds);
        criteria.and(FS_ID).is(fsId);
        criteria.and(TConstants.DEALER_ID).is(dealerId);
        return this.getMongoTemplate().find(Query.query(criteria), OemFsMapping.class);
    }

    @Override
    public List<OemFsMapping> findByGlAccountIdAndYearNonDeleted(List<String> glAccountIds, Integer year, Integer version, String dealerId, String oemId){
        Criteria criteria = new Criteria(TConstants.GLACCOUNT_ID).in(glAccountIds);
        criteria.and(TConstants.YEAR).is(year);
        criteria.and(TConstants.DEALER_ID).is(dealerId);
        criteria.and(OemFsMapping.OEM_ID).is(oemId);
        criteria.and(OemFsMapping.VERSION).is(version);
        criteria.and(DELETED).is(false);

        return this.getMongoTemplate().find(Query.query(criteria), OemFsMapping.class);
    }


    @Override
    public List<OemFsMapping> findMappingsByFsId(String fsId, String dealerId) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(FS_ID).is(fsId);
        criteria.and(TConstants.DEALER_ID).is(dealerId);
        return this.getMongoTemplate().find(Query.query(criteria), OemFsMapping.class);
    }

    @Override
    public List<OemFsMapping> findNonDeletedMappingsForOEMYearVersionByDealerIdAndSite(String oemId, Integer year, Integer version, String dealerId, String siteId) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(TConstants.YEAR).is(year);
        criteria.and(TConstants.DEALER_ID).is(dealerId);
        criteria.and(SITE_ID).is(siteId);
        criteria.and(OemFsMapping.OEM_ID).is(oemId);
        criteria.and(OemFsMapping.VERSION).is(version);
        Sort sort = Sort.by(Sort.Direction.DESC, TConstants.MODIFIED_TIME);

        return this.getMongoTemplate().find(Query.query(criteria).with(sort), OemFsMapping.class);
    }

    @Override
    public List<OemFsMapping> findMappingsForOEMYearVersionByDealerIdNonDeleted(String oemId, Integer year, Integer version, String dealerId) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(TConstants.YEAR).is(year);
        criteria.and(TConstants.DEALER_ID).is(dealerId);
        criteria.and(OemFsMapping.OEM_ID).is(oemId);
        criteria.and(OemFsMapping.VERSION).is(version);
        return this.getMongoTemplate().find(Query.query(criteria), OemFsMapping.class);
    }

    @Override
    public void softDeleteAllRecords(String dealerId, String tkOemId, Integer tkOemYear, Integer tkOemVersion) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(TConstants.YEAR).is(tkOemYear);
        criteria.and(TConstants.DEALER_ID).is(dealerId);
        criteria.and(OemFsMapping.OEM_ID).is(tkOemId);
        criteria.and(OemFsMapping.VERSION).is(tkOemVersion);

        Query q = Query.query(criteria);

        Update updateQ = new Update();
        updateQ.set(DELETED, true);
        updateQ.set(MODIFIED_TIME, System.currentTimeMillis());

        UpdateResult updateResult = this.getMongoTemplate().updateMulti(q, updateQ, OemFsMapping.class);

        log.info("OemFsMapping softDeleteAllRecords updateResult getMatchedCount: {}", updateResult.getMatchedCount() );
        log.info("OemFsMapping softDeleteAllRecords updateResult getModifiedCount: {}", updateResult.getModifiedCount() );

    }

    @Override
    public void delete(String tkOemId, Integer tkOemYear, Integer tkOemVersion, List<OemFsMapping> mappings, String dealerId, String siteId){

        if(TCollectionUtils.isEmpty(mappings)) return;

        Criteria criteria = criteriaForNonDeleted();
        criteria.and(TConstants.YEAR).is(tkOemYear);
        criteria.and(TConstants.DEALER_ID).is(dealerId);
        criteria.and(OemFsMapping.OEM_ID).is(tkOemId);
        criteria.and(OemFsMapping.VERSION).is(tkOemVersion);
        criteria.and(ID).in(mappings.stream().map(TBaseMongoBean::getId).collect(Collectors.toList()));

        Query q = Query.query(criteria);

        Update updateQ = new Update();
        updateQ.set(DELETED, true);
        updateQ.set(MODIFIED_TIME, System.currentTimeMillis());

        UpdateResult updateResult = this.getMongoTemplate().updateMulti(q, updateQ, OemFsMapping.class);
        log.info("OemFsMapping softDeleteRecords updateResult {}", JsonUtil.toJson(updateResult));
    }

    @Override
    public List<OemFsMapping> findMappingByGroupCodes(String oemId, Integer year, Integer version, String dealerId, List<String> groupCodes, String siteId){

        if(TCollectionUtils.isEmpty(groupCodes)) return new ArrayList<>();

        Criteria criteria = criteriaForNonDeleted();
        criteria.and(TConstants.YEAR).is(year);
        criteria.and(TConstants.DEALER_ID).is(dealerId);
        criteria.and(OemFsMapping.OEM_ID).is(oemId);
        criteria.and(OemFsMapping.VERSION).is(version);
        criteria.and(OemFsMapping.FS_CELL_GROUP_CODE).in(groupCodes);

        return this.getMongoTemplate().find(Query.query(criteria), OemFsMapping.class);
    }

    @Override
    public List<OemFsMapping> findAllByDealerIdIncludingDeleted() {
        Criteria criteria = new Criteria(TConstants.DEALER_ID).in(UserContextProvider.getCurrentDealerId());
        return this.getMongoTemplate().find(Query.query(criteria), OemFsMapping.class);
    }

    @Override
    public void hardDeleteMigratedOemFsMapping(String dealerId) {
        Criteria criteria = new Criteria(TConstants.DEALER_ID).is(dealerId);
        criteria.and(MIGRATED).is(true);
        getMongoTemplate().findAllAndRemove(Query.query(criteria), OemFsMapping.class);
    }

    @Override
    public List<OemFsMapping> findInBulkByGroupCodesAndOemsAndYear(Set<String> groupCodes, Set<String> oemIds, Integer year, String dealerId, String siteId) {
        if (TCollectionUtils.isEmpty(groupCodes) || TCollectionUtils.isEmpty(oemIds)) {
            return Lists.newArrayList();
        }
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(OemFsMapping.FS_CELL_GROUP_CODE).in(groupCodes);
        criteria.and(TConstants.YEAR).is(year);
        criteria.and(OemFsMapping.OEM_ID).in(oemIds);
        criteria.and(TConstants.DEALER_ID_KEY).is(dealerId);
        criteria.and(SITE_ID).is(siteId);
        Query query = Query.query(criteria);
        query.fields().include(OemFsMapping.FS_CELL_GROUP_CODE).include(OemFsMapping.GL_ACCT_ID).include(DEALER_ID).include(OemFsMapping.OEM_ID);
        return this.getMongoTemplate().find(query, OemFsMapping.class);
    }

    @Override
    public void updateDefaultSiteId(String dealerId) {
        Criteria criteria = criteriaForNonDeleted().and(DEALER_ID).is(dealerId);
        Update update = new Update();
        update.set(SITE_ID, getDefaultSiteId());
        update.set(TConstants.MODIFIED_TIME,System.currentTimeMillis());
        update.set(TConstants.MODIFIED_BY_USER_ID, UserContextProvider.getCurrentUserId());
        getMongoTemplate().updateMulti(Query.query(criteria), update, OemFsMapping.class);
    }

    @Override
    public void updateFsIdInOemFsMapping(FSEntry fsEntry) {
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
        getMongoTemplate().updateMulti(Query.query(criteria), update, OemFsMapping.class);
    }

    @Override
    public List<OemFsMapping> getFSEntriesByFsIdsAndDealerId(List<String> fsIds, String dealerId) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(FS_ID).in(fsIds);
        criteria.and(DEALER_ID).is(dealerId);
        return  this.findAll(criteria, this.getBeanClass(), this.getMongoTemplate());
    }

    @Override
    public void deleteOemFsMappingByIdAndDealerId(Set<String> id, String dealerId) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(ID).in(id);
        criteria.and(DEALER_ID).is(dealerId);

        Update update = new Update();
        update.set(DELETED, true);
        update.set(MODIFIED_TIME, System.currentTimeMillis());
        update.set(MODIFIED_BY_USER_ID, UserContextProvider.getCurrentUserId());
        UpdateResult updateResult = this.getMongoTemplate().updateMulti(Query.query(criteria), update, OemFsMapping.class);
        log.info("Deleted duplicate mappings from OemFsMapping {}", JsonUtil.toJson(updateResult));
    }

    @Override
    public List<OemFsMapping> findMappingsByGroupCodeAndFsIds(Collection<String> groupCodes, Collection<String> fsIds, String dealerId) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(DEALER_ID).is(dealerId);
        criteria.and(OemFsMapping.FS_CELL_GROUP_CODE).in(groupCodes);
        criteria.and(FS_ID).in(fsIds);
        return this.getMongoTemplate().find(Query.query(criteria), OemFsMapping.class);
    }


    @Override
    public void hardDeleteMappings(Collection<String> fsIds) {
        Criteria criteria =  Criteria.where(FS_ID).in(fsIds);
        this.getMongoTemplate().remove(Query.query(criteria), OemFsMapping.class);
    }

    @Override
    public Integer addTenantId(){
        BulkOperations bulkOperations= TMongoUtils.addTenantIdInMongoBean(getMongoTemplate(), OemFsMapping.class);
        return bulkOperations.execute().getModifiedCount();
    }

    @Override
    public List<OemFsMapping> getMappingsByOemIds(List<String> fsIds, Collection<OemFsGroupCodeDetails> details) {
        Query query = new Query();
        if (TCollectionUtils.isEmpty(details)) {
            return new ArrayList<>();
        }
        Criteria criteria = criteriaForNonDeleted();
        List<Criteria> criteriaList = new ArrayList<>();
        for (OemFsGroupCodeDetails fsGroupCodeDetails : details) {
            Criteria expression = criteriaForNonDeleted();
            expression.and(OemFsMapping.OEM_ID).is(fsGroupCodeDetails.getOemId());
            expression.and(FS_ID).in(fsIds);
            expression.and(OemFsMapping.FS_CELL_GROUP_CODE).in(fsGroupCodeDetails.getGroupCodes());
            criteriaList.add(expression);
        }
        query.addCriteria(criteria.orOperator(criteriaList.toArray(new Criteria[criteriaList.size()])));
        return this.getMongoTemplate().find(query, OemFsMapping.class);
    }

}
