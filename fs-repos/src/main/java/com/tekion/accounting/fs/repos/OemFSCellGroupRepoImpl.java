package com.tekion.accounting.fs.repos;

import com.google.common.collect.Sets;
import com.mongodb.BasicDBObject;
import com.mongodb.bulk.BulkWriteUpsert;
import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.beans.common.AccountingOemFsCellGroup;
import com.tekion.accounting.fs.beans.common.OemTemplate;
import com.tekion.accounting.fs.common.utils.TMongoUtils;
import com.tekion.core.mongo.BaseGlobalMongoRepository;
import com.tekion.core.serverconfig.beans.ModuleName;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.UserContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.tekion.accounting.fs.common.TConstants.*;


@Slf4j
@Component
public class OemFSCellGroupRepoImpl extends BaseGlobalMongoRepository<AccountingOemFsCellGroup> implements OemFsCellGroupRepo {

    public OemFSCellGroupRepoImpl() {
        super(ModuleName.UNIVERSAL_GLOBAL.name(), AccountingOemFsCellGroup.class);
    }

    @Override
    public List<AccountingOemFsCellGroup> findByOemId(String oemId, String country) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(OemTemplate.OEM_ID).is(oemId);
        criteria.and(COUNTRY).is(country);
        return this.getMongoTemplate().find(Query.query(criteria), AccountingOemFsCellGroup.class);
    }

    @Override
    public List<AccountingOemFsCellGroup> findByOemIdAndGroupDisplayNames(String oemId, List<String> groupDisplayNames, String country) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(OemTemplate.OEM_ID).is(oemId);
        criteria.and(AccountingOemFsCellGroup.GROUP_DISPLAY_NAME).in(groupDisplayNames);
        criteria.and(COUNTRY).is(country);
        return this.getMongoTemplate().find(Query.query(criteria), AccountingOemFsCellGroup.class);
    }

    @Override
    public List<AccountingOemFsCellGroup> findByGroupCodes(String oemId, Integer year, Integer version, List<String> groupCodes, String country) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(AccountingOemFsCellGroup.OEM_ID).is(oemId);
        criteria.and(AccountingOemFsCellGroup.YEAR).is(year);
        criteria.and(AccountingOemFsCellGroup.VERSION).is(version);
        criteria.and(AccountingOemFsCellGroup.GROUP_CODE).in(groupCodes);
        criteria.and(COUNTRY).is(country);
        return this.getMongoTemplate().find(Query.query(criteria), AccountingOemFsCellGroup.class);
    }

    @Override
    public void addCountryInOemFsCellGroupCodes() {
        Criteria criteria = criteriaForNonDeleted();
        Query q = new Query(criteria);
        Update update = new Update();
        update.set(COUNTRY, COUNTRY_US);
        update.set(MODIFIED_TIME, System.currentTimeMillis());
        update.set(MODIFIED_BY_USER_ID, UserContextProvider.getCurrentUserId());
        getMongoTemplate().updateMulti(q, update, AccountingOemFsCellGroup.class);
    }

    @Override
    public List<AccountingOemFsCellGroup> findByOemId(String oemId, int year, String country) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(OemTemplate.OEM_ID).is(oemId);
        criteria.and(OemTemplate.YEAR).is(year);
        criteria.and(COUNTRY).is(country);
        return this.getMongoTemplate().find(Query.query(criteria), AccountingOemFsCellGroup.class);
    }

    @Override
    public List<AccountingOemFsCellGroup> findNonDeletedByOemIdYearVersionAndCountry(String oemId, Integer year, Integer version, String country) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(OemTemplate.OEM_ID).is(oemId);
        criteria.and(TConstants.YEAR).is(year);
        criteria.and(AccountingOemFsCellGroup.VERSION).is(version);
        criteria.and(COUNTRY).is(country);
        return this.getMongoTemplate().find(Query.query(criteria), AccountingOemFsCellGroup.class);
    }

    @Override
    public List<AccountingOemFsCellGroup> findByOemIdsAndYearNonDeleted(Set<String> oemIds, Integer year, String country) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(TConstants.YEAR).is(year);
        if (!TCollectionUtils.isEmpty(oemIds)) {
            criteria.and(OemTemplate.OEM_ID).in(oemIds);
        }
        criteria.and(COUNTRY).is(country);
        return this.getMongoTemplate().find(Query.query(criteria), AccountingOemFsCellGroup.class);
    }

    @Override
    public List<AccountingOemFsCellGroup> findCellGroupByIds(List<String> ids) {
        if (TCollectionUtils.isEmpty(ids)) return null;

        Criteria c = Criteria.where(TConstants.ID).in(ids);
        return this.getMongoTemplate().find(Query.query(c), AccountingOemFsCellGroup.class);
    }

    @Override
    public List<BulkWriteUpsert> delete(List<AccountingOemFsCellGroup> groupCodes) {
        if(TCollectionUtils.isEmpty(groupCodes)){
            return null;
        }
        BulkOperations bulkOperations = getMongoTemplate().bulkOps(BulkOperations.BulkMode.ORDERED, AccountingOemFsCellGroup.class);
        long time = System.currentTimeMillis();
        groupCodes.forEach(gc -> {
            if( Objects.isNull(gc.getId())){
                return;
            }

            Query query = Query.query(Criteria.where(TConstants.ID).in(gc.getId()));
            Update update = new Update();

            update.set(AccountingOemFsCellGroup.GROUP_CODE, appendTimeStamp(gc.getGroupCode()));
            update.set(TConstants.DELETED, true);
            update.set(MODIFIED_TIME, time);
            bulkOperations.upsert(query, update);
        });
        return bulkOperations.execute().getUpserts();
    }

    private String appendTimeStamp(String groupCode) {
        return groupCode + "_" + System.currentTimeMillis();
    }

    @Override
    public List<BulkWriteUpsert> upsertBulk(List<AccountingOemFsCellGroup> groupCodes) {
        if (TCollectionUtils.isEmpty(groupCodes)) {
            return null;
        }
        BulkOperations bulkOperations = getMongoTemplate().bulkOps(BulkOperations.BulkMode.ORDERED, AccountingOemFsCellGroup.class);
        groupCodes.forEach(groupCode -> {
            Query query = Query.query(
                    Criteria.where(AccountingOemFsCellGroup.GROUP_CODE).is(groupCode.getGroupCode()).
                        and(AccountingOemFsCellGroup.YEAR).is(groupCode.getYear()).
                        and(AccountingOemFsCellGroup.VERSION).is(groupCode.getVersion()).
                        and(AccountingOemFsCellGroup.OEM_ID).is(groupCode.getOemId()).
                        and(COUNTRY).is(groupCode.getCountry())
            );
            BasicDBObject dbDoc = new BasicDBObject();
            getMongoTemplate().getConverter().write(groupCode, dbDoc);
            Update update = TMongoUtils.fromDBObjectExcludeNullFields(dbDoc, Sets.newHashSet("_id", "id"));
            bulkOperations.upsert(query, update);
        });
        return bulkOperations.execute().getUpserts();
    }
}
