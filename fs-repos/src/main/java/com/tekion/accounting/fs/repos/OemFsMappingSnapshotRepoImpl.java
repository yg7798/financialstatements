package com.tekion.accounting.fs.repos;

import com.google.common.collect.Lists;
import com.mongodb.bulk.BulkWriteUpsert;
import com.tekion.accounting.fs.TConstants;
import com.tekion.accounting.fs.master.beans.FSEntry;
import com.tekion.accounting.fs.master.beans.OEMFinancialMapping;
import com.tekion.accounting.fs.master.beans.OemFsMapping;
import com.tekion.accounting.fs.master.beans.OemFsMappingSnapshot;
import com.tekion.core.mongo.BaseDealerLevelMongoRepository;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.UserContextProvider;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.tekion.accounting.fs.TConstants.*;
import static com.tekion.accounting.fs.master.beans.FSEntry.FS_ID;
import static com.tekion.accounting.fs.master.beans.OemTemplate.OEM_ID;
import static com.tekion.accounting.fs.utils.UserContextUtils.getDefaultSiteId;


@Component
public class OemFsMappingSnapshotRepoImpl  extends BaseDealerLevelMongoRepository<OemFsMappingSnapshot> implements OemFsMappingSnapshotRepo {

    public OemFsMappingSnapshotRepoImpl() {
        super(TENANT_DEFAULT , OemFsMappingSnapshot.class);
    }

    @Override
    public void saveBulkSnapshot(List<OemFsMappingSnapshot> oemFsMappingSnapshots) {
        super.insertBulk(oemFsMappingSnapshots);
    }

    @Override
    public OemFsMappingSnapshot findOneSnapshotByYearAndMonth(String fsId, int month, String dealerId) {
        return findOneSnapshot(fsId, Collections.singletonList(month), dealerId);
    }

    @Override
    public OemFsMappingSnapshot findOneSnapshot(String fsId, List<Integer> months, String dealerId){
        if(TCollectionUtils.isEmpty(months)) return null;

        Criteria criteria = Criteria
                .where(TConstants.DELETED).is(false)
                .and(FS_ID).is(fsId)
                .and(TConstants.MONTH).in(months)
                .and(TConstants.DEALER_ID).is(dealerId);
        return this.getMongoTemplate().findOne(Query.query(criteria), OemFsMappingSnapshot.class);
    }


    @Override
    public List<OemFsMappingSnapshot> findAllSnapshotsUntilMonth(String fsId, int untilMonth, Collection<String> groupCodes, String dealerId) {
        Criteria criteria = Criteria
                .where(TConstants.DELETED).is(false)
                .and(FS_ID).is(fsId)
                .and(TConstants.MONTH).lte(untilMonth)
                .and(TConstants.DEALER_ID).is(dealerId)
                .and(OemFsMapping.FS_CELL_GROUP_CODE).in(groupCodes);
        return this.getMongoTemplate().find(Query.query(criteria), OemFsMappingSnapshot.class);
    }

    @Override
    public List<OemFsMappingSnapshot> findAllSnapshotByYearAndMonth(String fsId, int month, String dealerId) {
        Criteria criteria = criteriaForNonDeleted()
                .and(FS_ID).is(fsId)
                .and(TConstants.MONTH).is(month)
                .and(TConstants.DEALER_ID).is(dealerId);
        return this.getMongoTemplate().find(Query.query(criteria), OemFsMappingSnapshot.class);
    }
    @Override
    public List<OemFsMappingSnapshot> findAllSnapshotsByYearVersionOemId(String oemId, Integer version, int year){
        Criteria criteria = Criteria
                .where(TConstants.DELETED).is(false)
                .and(OEMFinancialMapping.OEM_ID).is(oemId)
                .and(TConstants.DEALER_ID).is(UserContextProvider.getCurrentDealerId())
                .and(OEMFinancialMapping.YEAR).is(year)
                .and(VERSION).is(version);
        return this.getMongoTemplate().find(Query.query(criteria), OemFsMappingSnapshot.class);
    }

    @Override
    public List<OemFsMappingSnapshot> findAllSnapshotsInTimeRangeAndOemsAndGroupCodes(Set<String> groupCodes, Set<String> oemIds, Integer fromYear, Integer toYear, Integer fromMonth, Integer toMonth, String siteId) {
        if (TCollectionUtils.isEmpty(groupCodes) || TCollectionUtils.isEmpty(oemIds)) {
            return Lists.newArrayList();
        }
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(OemFsMapping.FS_CELL_GROUP_CODE).in(groupCodes);
        criteria.and(OemFsMapping.OEM_ID).in(oemIds);
        criteria.and(TConstants.DEALER_ID_KEY).is(UserContextProvider.getCurrentDealerId());
        criteria.and(SITE_ID).is(siteId);
        if (fromYear.equals(toYear)) {
            criteria.and(TConstants.MONTH).gte(fromMonth).lte(toMonth);
        } else {
            Criteria criteria1 = Criteria.where(TConstants.YEAR).is(fromYear).and(TConstants.MONTH).gte(fromMonth);
            Criteria criteria2 = Criteria.where(TConstants.YEAR).is(toYear).and(TConstants.MONTH).lte(toMonth);
            Criteria orCriteria = new Criteria().orOperator(criteria1, criteria2);
            criteria.andOperator(orCriteria);
        }
        Query query = Query.query(criteria);
        return this.getMongoTemplate().find(query, OemFsMappingSnapshot.class);
    }

    @Override
    public void deleteSnapshotByYearAndMonth(String fsId, int month, String dealerId) {
        deleteSnapshots(fsId, Collections.singletonList(month), dealerId);
    }

    @Override
    public void deleteSnapshots(String fsId, List<Integer> months, String dealerId){

        if(TCollectionUtils.isEmpty(months)) return;

        Criteria criteria = Criteria
            .where(TConstants.DELETED).is(false)
            .and(FS_ID).is(fsId)
            .and(TConstants.MONTH).in(months)
            .and(TConstants.DEALER_ID).is(dealerId);

        Update update = new Update();
        update.set(TConstants.DELETED, true);
        update.set(TConstants.MODIFIED_TIME, System.currentTimeMillis());
        this.getMongoTemplate().updateMulti(Query.query(criteria), update, OemFsMappingSnapshot.class);
    }

    @Override
    public void updateDefaultSiteId(String dealerId) {
        Criteria criteria = criteriaForNonDeleted().and(DEALER_ID).is(dealerId);
        Update update = new Update();
        update.set(SITE_ID, getDefaultSiteId());
        update.set(TConstants.MODIFIED_TIME,System.currentTimeMillis());
        update.set(TConstants.MODIFIED_BY_USER_ID, UserContextProvider.getCurrentUserId());
        getMongoTemplate().updateMulti(Query.query(criteria), update, OemFsMappingSnapshot.class);
    }


    @Override
    public void updateFsIdInOemFsMappingSnapshots(FSEntry fsEntry) {
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
        getMongoTemplate().updateMulti(Query.query(criteria), update, OemFsMappingSnapshot.class);
    }

    @Override
    public List<BulkWriteUpsert> updateSiteIdInBulk(Collection<OemFsMappingSnapshot> oemFsMappingSnapshots , String dealerId) {
        if(TCollectionUtils.isEmpty(oemFsMappingSnapshots)){
            return null;
        }
        BulkOperations bulkOperations = getMongoTemplate().bulkOps(BulkOperations.BulkMode.ORDERED, OemFsMappingSnapshot.class);
        oemFsMappingSnapshots.forEach(oemFsCellCodeSnapshot -> {
            Query query = Query.query(Criteria.where(TConstants.ID).in(oemFsCellCodeSnapshot.getId()));
            Update update = new Update();
            update.set(TConstants.MODIFIED_TIME,System.currentTimeMillis());
            update.set(SITE_ID,oemFsCellCodeSnapshot.getSiteId());
            update.set(TConstants.MODIFIED_BY_USER_ID, UserContextProvider.getCurrentUserId());
            bulkOperations.upsert(query,update);
        });
        return bulkOperations.execute().getUpserts();
    }
}
