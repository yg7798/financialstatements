package com.tekion.accounting.fs.repos.worksheet;

import com.google.common.collect.Lists;
import com.mongodb.bulk.BulkWriteUpsert;
import com.tekion.accounting.fs.TConstants;
import com.tekion.accounting.fs.beans.FSEntry;
import com.tekion.accounting.fs.beans.mappings.OemFsMapping;
import com.tekion.accounting.fs.beans.memo.HCWorksheet;
import com.tekion.core.mongo.BaseDealerLevelMongoRepository;
import com.tekion.core.serverconfig.beans.ModuleName;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.UserContextProvider;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

import static com.tekion.accounting.fs.TConstants.DEALER_ID;
import static com.tekion.accounting.fs.TConstants.SITE_ID;
import static com.tekion.accounting.fs.beans.FSEntry.FS_ID;
import static com.tekion.accounting.fs.beans.OemTemplate.OEM_ID;
import static com.tekion.accounting.fs.utils.UserContextUtils.getDefaultSiteId;


@Component
public class HCWorksheetRepoImpl extends BaseDealerLevelMongoRepository<HCWorksheet> implements HCWorksheetRepo {

    public HCWorksheetRepoImpl(){
        super( ModuleName.TENANT_DEFAULT.name() , HCWorksheet.class);
    }

    @Override
    public List<HCWorksheet> findForOemByYear(String oemId, int year, int version, String dealerId, String siteId) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(DEALER_ID).is(dealerId);
        criteria.and(SITE_ID).is(siteId);
        criteria.and(OEM_ID).is(oemId);
        criteria.and(TConstants.YEAR).is(year);
        criteria.and(TConstants.VERSION).is(version);
        return this.getMongoTemplate().find(Query.query(criteria),HCWorksheet.class);
    }

    @Override
    public List<HCWorksheet> findByIds(Collection<String> ids, String dealerId, String siteId) {
        if(TCollectionUtils.isEmpty(ids)){
            return Lists.newArrayList();
        }
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(DEALER_ID).is(dealerId);
        criteria.and(SITE_ID).is(siteId);
        criteria.and(TConstants.ID).in(ids);
        return this.getMongoTemplate().find(Query.query(criteria),HCWorksheet.class);
    }

    @Override
    public List<HCWorksheet> findByFsId(String fsId) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(FS_ID).is(fsId);
        criteria.and(DEALER_ID).is(UserContextProvider.getCurrentDealerId());
        return this.getMongoTemplate().find(Query.query(criteria),HCWorksheet.class);
    }

    @Override
    public List<BulkWriteUpsert> updateBulk(Collection<HCWorksheet> hcWorksheets, String dealerId) {
        if(TCollectionUtils.isEmpty(hcWorksheets)){
            return null;
        }
        BulkOperations bulkOperations = getMongoTemplate().bulkOps(BulkOperations.BulkMode.ORDERED, HCWorksheet.class);
        hcWorksheets.forEach(hcWorksheet -> {
            Query query = Query.query(Criteria.where(TConstants.ID).in(hcWorksheet.getId()));
            Update update = new Update();
            update.set(HCWorksheet.VALUES,hcWorksheet.getValues());
            update.set(TConstants.MODIFIED_TIME,System.currentTimeMillis());
            update.set(SITE_ID,hcWorksheet.getSiteId());
            update.set(TConstants.MODIFIED_BY_USER_ID, UserContextProvider.getCurrentUserId());
            bulkOperations.upsert(query,update);
        });
        return bulkOperations.execute().getUpserts();
    }

    @Override
    public void updateDefaultSiteId(String dealerId) {
        Criteria criteria = criteriaForNonDeleted().and(DEALER_ID).is(dealerId);
        Update update = new Update();
        update.set(SITE_ID, getDefaultSiteId());
        update.set(TConstants.MODIFIED_TIME,System.currentTimeMillis());
        update.set(TConstants.MODIFIED_BY_USER_ID, UserContextProvider.getCurrentUserId());
        getMongoTemplate().updateMulti(Query.query(criteria), update, HCWorksheet.class);
    }

    @Override
    public void updateFsIdInHCWorksheets(FSEntry fsEntry) {
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
        getMongoTemplate().updateMulti(Query.query(criteria), update, HCWorksheet.class);
    }

    @Override
    public List<HCWorksheet> findByOemIdYearVersion(String oemId, int year, int version) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(DEALER_ID).is(UserContextProvider.getCurrentDealerId());
        criteria.and(OEM_ID).is(oemId);
        criteria.and(TConstants.YEAR).is(year);
        criteria.and(TConstants.VERSION).is(version);
        return this.getMongoTemplate().find(Query.query(criteria),HCWorksheet.class);
    }
}
