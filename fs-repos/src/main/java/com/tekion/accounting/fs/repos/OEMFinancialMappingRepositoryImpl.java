package com.tekion.accounting.fs.repos;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.bulk.BulkWriteResult;
import com.tekion.accounting.fs.TConstants;
import com.tekion.accounting.fs.beans.mappings.OEMFinancialMapping;
import com.tekion.accounting.fs.beans.mappings.OemFsMapping;
import com.tekion.accounting.fs.utils.TMongoUtils;
import com.tekion.core.mongo.BaseDealerLevelMongoRepository;
import com.tekion.core.utils.UserContextProvider;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static com.tekion.accounting.fs.TConstants.*;
import static com.tekion.accounting.fs.beans.FSEntry.FS_ID;
import static com.tekion.accounting.fs.utils.TMongoUtils.modifyKeyNameForDelete;
import static com.tekion.accounting.fs.utils.UserContextUtils.getDefaultSiteId;
import static com.tekion.core.utils.TCollectionUtils.nullSafeList;

@Component
public class OEMFinancialMappingRepositoryImpl extends BaseDealerLevelMongoRepository<OEMFinancialMapping> implements OEMFinancialMappingRepository {

    public OEMFinancialMappingRepositoryImpl() {
        super(TENANT_DEFAULT , OEMFinancialMapping.class);
    }

    @Override
    public List<OEMFinancialMapping> findMappingsForGLAccount(String accountId, String dealerId) {
        return this.getMongoTemplate().find(Query.query(Criteria.where(OEMFinancialMapping.GL_ACCOUNT_ID).is(accountId)
                .and(DELETED).is(false).and(DEALER_ID_KEY).is(dealerId)
        ), OEMFinancialMapping.class);
    }

    @Override
    public BulkWriteResult deleteMappings(List<OEMFinancialMapping> mappings) {
        List<Pair<Query, Update>> updates = Lists.newArrayList();
        BulkOperations bulkOperations = getMongoTemplate().bulkOps(BulkOperations.BulkMode.ORDERED, OEMFinancialMapping.class);
        mappings.stream().filter(Objects::nonNull).forEach(mapping -> {
            Query query = Query.query(TMongoUtils.getDealerIdFilterCriteria().and(TConstants.ID).is(mapping.getId()));
            Update update = new Update();
            update.set(TConstants.DELETED, true);
            update.set(OEMFinancialMapping.GL_ACCOUNT_ID, modifyKeyNameForDelete(mapping.getGlAccountId()));
            update.set(OEMFinancialMapping.YEAR, modifyKeyNameForDelete(mapping.getYear()));
            update.set(OEMFinancialMapping.OEM_ID, modifyKeyNameForDelete(mapping.getOemId()));
            updates.add(Pair.of(query, update));
        });
        bulkOperations.upsert(updates);
        return bulkOperations.execute();
    }

    public BulkWriteResult upsertMappings(List<OEMFinancialMapping> mappings) {
        List<Pair<Query, Update>> updates = Lists.newArrayList();
        BulkOperations bulkOperations = getMongoTemplate().bulkOps(BulkOperations.BulkMode.ORDERED, OEMFinancialMapping.class);
        nullSafeList(mappings).stream().filter(Objects::nonNull).forEach(mapping -> {
            Query query = Query.query(TMongoUtils.getDealerIdFilterCriteria().and(ID).is(mapping.getId()));
            BasicDBObject dbDoc = new BasicDBObject();
            getMongoTemplate().getConverter().write(mapping, dbDoc);
            Update update = TMongoUtils.fromDBObjectExcludeNullFields(dbDoc);
            updates.add(Pair.of(query, update));
        });
        bulkOperations.upsert(updates);
        return bulkOperations.execute();
    }
    @Override
    public List<OEMFinancialMapping> findMappingsByFsIdAndDealerId(String fsId, String dealerId) {
        Criteria criteria = Criteria
            .where(TConstants.DELETED).is(false)
            .and(FS_ID).is(fsId)
            .and(DEALER_ID).is(dealerId);

        Sort sort = Sort.by(Direction.DESC, TConstants.MODIFIED_TIME);
        return this.getMongoTemplate().find(Query.query(criteria).with(sort), OEMFinancialMapping.class);
    }

    @Override
    public OemFsMapping save(OemFsMapping oemFsMapping) {
        return getMongoTemplate().save(oemFsMapping);
        }

    @Override
    public void updateDefaultSiteId(String dealerId) {
        Criteria criteria = criteriaForNonDeleted().and(DEALER_ID).is(dealerId);
        Update update = new Update();
        update.set(TConstants.SITE_ID, getDefaultSiteId());
        update.set(TConstants.MODIFIED_TIME,System.currentTimeMillis());
        update.set(TConstants.MODIFIED_BY_USER_ID, UserContextProvider.getCurrentUserId());
        getMongoTemplate().updateMulti(Query.query(criteria), update, OEMFinancialMapping.class);
    }
}
