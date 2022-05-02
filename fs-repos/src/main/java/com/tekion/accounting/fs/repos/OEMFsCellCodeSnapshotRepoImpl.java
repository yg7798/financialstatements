package com.tekion.accounting.fs.repos;

import com.mongodb.bulk.BulkWriteUpsert;
import com.tekion.accounting.fs.beans.accountingInfo.AccountingInfo;
import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.beans.common.OEMFsCellCodeSnapshot;
import com.tekion.accounting.fs.beans.mappings.OEMFinancialMapping;
import com.tekion.accounting.fs.beans.mappings.OemFsMapping;
import com.tekion.accounting.fs.common.utils.TMongoUtils;
import com.tekion.accounting.fs.enums.FSType;
import com.tekion.core.mongo.BaseDealerLevelMongoRepository;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.UserContextProvider;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.tekion.accounting.fs.common.TConstants.*;
import static com.tekion.accounting.fs.beans.common.FSEntry.FS_ID;
import static com.tekion.accounting.fs.beans.common.FSEntry.FS_TYPE;
import static com.tekion.accounting.fs.beans.common.OemTemplate.OEM_ID;
import static com.tekion.accounting.fs.common.utils.UserContextUtils.getDefaultSiteId;

@Component
public class OEMFsCellCodeSnapshotRepoImpl extends BaseDealerLevelMongoRepository<OEMFsCellCodeSnapshot> implements OEMFsCellCodeSnapshotRepo {

    private final String TIMESTAMP = "timestamp";
    public OEMFsCellCodeSnapshotRepoImpl() {
        super(TENANT_DEFAULT , OEMFsCellCodeSnapshot.class);
    }

    @Override
    public void saveBulkSnapshot(List<OEMFsCellCodeSnapshot> oemExecutiveSnapshotList) {
        super.insertBulk(oemExecutiveSnapshotList);
    }

    @Override
    public List<OEMFsCellCodeSnapshot> findSnapshotByCodesAndMonth(String fsId, Integer month, Set<String> codes, String dealerId) {
        Criteria criteria = Criteria
                .where(TConstants.DELETED).is(false)
                .and(TConstants.DEALER_ID).is(dealerId)
                .and(FS_ID).is(fsId)
                .and(CODE).in(codes)
                .and(TConstants.MONTH).is(month);

        return this.getMongoTemplate().find(Query.query(criteria), OEMFsCellCodeSnapshot.class);
    }

    @Override
    public List<OEMFsCellCodeSnapshot> findAllSnapshotByYearAndMonth(String oemId, Integer version, int year, int month, String dealerId, String siteId) {
        Criteria criteria = Criteria
                .where(TConstants.DELETED).is(false)
                .and(OEMFinancialMapping.OEM_ID).is(oemId)
                .and(TConstants.MONTH).is(month)
                .and(TConstants.DEALER_ID).is(dealerId)
                .and(SITE_ID).is(siteId)
                .and(TConstants.VERSION).is(version)
                .and(OEMFinancialMapping.YEAR).is(year);

        return this.getMongoTemplate().find(Query.query(criteria), OEMFsCellCodeSnapshot.class);
    }

    @Override
    public List<OEMFsCellCodeSnapshot> findAllSnapshotByYearOemIdVersion(String oemId,int year, Integer version, String dealerId){
        Criteria criteria = Criteria
                .where(TConstants.DELETED).is(false)
                .and(OEMFinancialMapping.OEM_ID).is(oemId)
                .and(TConstants.DEALER_ID).is(dealerId)
                .and(TConstants.VERSION).is(version)
                .and(OEMFinancialMapping.YEAR).is(year);

        return this.getMongoTemplate().find(Query.query(criteria), OEMFsCellCodeSnapshot.class);
    }


    @Override
    public List<OEMFsCellCodeSnapshot> findAllSnapshotByFsIdAndMonth(String fsId, int month, String dealerId) {
        Criteria criteria = Criteria
                .where(TConstants.DELETED).is(false)
                .and(FS_ID).is(fsId)
                .and(TConstants.MONTH).is(month)
                .and(TConstants.DEALER_ID).is(dealerId);

        return this.getMongoTemplate().find(Query.query(criteria), OEMFsCellCodeSnapshot.class);
    }

    @Override
    public OEMFsCellCodeSnapshot findOneSnapshotByFsIdAndMonth(String fsId, int month, String dealerId) {
        Criteria criteria = Criteria
                .where(TConstants.DELETED).is(false)
                .and(FS_ID).is(fsId)
                .and(TConstants.MONTH).is(month)
                .and(TConstants.DEALER_ID).is(dealerId);

        return this.getMongoTemplate().findOne(Query.query(criteria), OEMFsCellCodeSnapshot.class);
    }

    @Override
    public void deleteSnapshotByFsIdAndMonth(String fsId, int month, String dealerId) {
        Criteria criteria = Criteria.where(TConstants.DELETED).is(false)
                .and(FS_ID).is(fsId)
                .and(TConstants.MONTH).is(month)
                .and(TConstants.DEALER_ID).is(dealerId);
        Update update = new Update();
        update.set(TConstants.DELETED, true);

        this.getMongoTemplate().updateMulti(Query.query(criteria), update, OEMFsCellCodeSnapshot.class);
    }

    @Override
    public void hardDeleteSnapshotsInBulk(Collection<String> fsIds, List<Integer> months, String dealerId) {
        Criteria criteria = Criteria.where(TConstants.DELETED).is(false)
                .and(FS_ID).in(fsIds)
                .and(TConstants.MONTH).in(months)
                .and(TConstants.DEALER_ID).is(dealerId);

        this.getMongoTemplate().remove(Query.query(criteria), OEMFsCellCodeSnapshot.class);
    }

    @Override
    public void hardDeleteSnapshotByFsIdAndMonth(Collection<String> fsIds, String dealerId) {
        Criteria criteria = Criteria.where(TConstants.DELETED).is(false)
                .and(FS_ID).in(fsIds)
                .and(TConstants.DEALER_ID).is(dealerId);

        this.getMongoTemplate().remove(Query.query(criteria), OEMFsCellCodeSnapshot.class);
    }

    @Override
    public void deleteBulkSnapshotByYearAndMonth(String oemId, int year, int fromMonth, int toMonth, String dealerId, String siteId) {
        Criteria criteria = Criteria
                .where(OEMFinancialMapping.OEM_ID).is(oemId)
                .and(TConstants.MONTH).gte(fromMonth).lte(toMonth)
                .and(TConstants.DEALER_ID).is(dealerId)
                .and(SITE_ID).is(siteId)
                .and(TConstants.DELETED).is(false)
                .and(OEMFinancialMapping.YEAR).is(year);

        Update update = new Update();
        update.set(TConstants.DELETED, true);

        this.getMongoTemplate().updateMulti(Query.query(criteria), update, OEMFsCellCodeSnapshot.class);
    }

    @Override
    public void updateDefaultSiteId(String dealerId) {
        Criteria criteria = criteriaForNonDeleted().and(DEALER_ID).is(dealerId);
        Update update = new Update();
        update.set(SITE_ID, getDefaultSiteId());
        update.set(TConstants.MODIFIED_TIME,System.currentTimeMillis());
        update.set(TConstants.MODIFIED_BY_USER_ID, UserContextProvider.getCurrentUserId());
        getMongoTemplate().updateMulti(Query.query(criteria), update, OEMFsCellCodeSnapshot.class);
    }

    @Override
    public void updateFsIdInOemFsCellCodeSnapshots(FSEntry fsEntry) {
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
        getMongoTemplate().updateMulti(Query.query(criteria), update, OEMFsCellCodeSnapshot.class);
    }

    @Override
    public List<BulkWriteUpsert> updateSiteIdInBulk(Collection<OEMFsCellCodeSnapshot> oemFsCellCodeSnapshots , String dealerId) {
        if(TCollectionUtils.isEmpty(oemFsCellCodeSnapshots)){
            return null;
        }
        BulkOperations bulkOperations = getMongoTemplate().bulkOps(BulkOperations.BulkMode.ORDERED, OEMFsCellCodeSnapshot.class);
        oemFsCellCodeSnapshots.forEach(oemFsCellCodeSnapshot -> {
            Query query = Query.query(Criteria.where(TConstants.ID).in(oemFsCellCodeSnapshot.getId()));
            Update update = new Update();
            update.set(TConstants.MODIFIED_TIME,System.currentTimeMillis());
            update.set(SITE_ID,oemFsCellCodeSnapshot.getSiteId());
            update.set(TConstants.MODIFIED_BY_USER_ID, UserContextProvider.getCurrentUserId());
            bulkOperations.upsert(query,update);
        });
        return bulkOperations.execute().getUpserts();
    }

    @Override
    public List<OEMFsCellCodeSnapshot> findSnapshotByCodes(String fsId, Set<String> codes, String dealerId){
        Criteria criteria = Criteria
                .where(TConstants.DELETED).is(false)
                .and(TConstants.DEALER_ID).is(dealerId)
                .and(FS_ID).is(fsId)
                .and(CODE).in(codes);

        return this.getMongoTemplate().find(Query.query(criteria), OEMFsCellCodeSnapshot.class);
    }

    @Override
    public void updateFsTypeInFsCellCodeSnapshots(FSEntry fsEntry){
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(DEALER_ID).is(fsEntry.getDealerId());
        criteria.and(FS_ID).is(fsEntry.getId());

        Update update = new Update();
        update.set(FS_TYPE, fsEntry.getFsType());
        update.set(TConstants.MODIFIED_TIME,System.currentTimeMillis());
        update.set(TConstants.MODIFIED_BY_USER_ID, UserContextProvider.getCurrentUserId());
        getMongoTemplate().updateMulti(Query.query(criteria), update, OEMFsCellCodeSnapshot.class);
    }

    @Override
    public List<OEMFsCellCodeSnapshot> getFsCellCodeByTimestamp(long fromTimestamp, long toTimestamp, Set<String> codes,
                                                                String oemId, String dealerId, String siteId) {

        Criteria criteria = Criteria
                .where(TIMESTAMP).gte(fromTimestamp)
                .lte(toTimestamp)
                .and(OEMFinancialMapping.OEM_ID).is(oemId)
                .and(DEALER_ID_KEY).is(dealerId)
                .and(SITE_ID).is(siteId)
                .and(TConstants.DELETED).is(false)
                .and(CODE).in(codes)
                .and(FS_TYPE).is(FSType.OEM.name());

        return this.getMongoTemplate().find(Query.query(criteria), OEMFsCellCodeSnapshot.class);
    }

    @Override
    public Integer addTenantId(){
        BulkOperations bulkOperations= TMongoUtils.addTenantIdInMongoBean(getMongoTemplate(), OEMFsCellCodeSnapshot.class);
        return bulkOperations.execute().getModifiedCount();
    }
}