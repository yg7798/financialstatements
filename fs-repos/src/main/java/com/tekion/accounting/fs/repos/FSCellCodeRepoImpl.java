package com.tekion.accounting.fs.repos;

import com.amazonaws.services.guardduty.model.Country;
import com.google.common.collect.Sets;
import com.mongodb.BasicDBObject;
import com.mongodb.bulk.BulkWriteUpsert;
import com.tekion.accounting.fs.common.TConstants;
import com.tekion.accounting.fs.beans.common.AccountingOemFsCellCode;
import com.tekion.accounting.fs.common.utils.TMongoUtils;
import com.tekion.core.mongo.BaseGlobalMongoRepository;
import com.tekion.core.serverconfig.beans.ModuleName;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.TStringUtils;
import com.tekion.core.utils.UserContextProvider;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static com.tekion.accounting.fs.beans.common.AccountingOemFsCellCode.OEM_ID;
import static com.tekion.accounting.fs.common.TConstants.*;

@Component
public class FSCellCodeRepoImpl extends BaseGlobalMongoRepository<AccountingOemFsCellCode> implements FSCellCodeRepo {

    public FSCellCodeRepoImpl() {
        super(ModuleName.UNIVERSAL_GLOBAL.name(), AccountingOemFsCellCode.class);
    }

    @Override
    public List<AccountingOemFsCellCode> getFsCellCodesForOemYearAndCountry(String oemId, Integer year, Integer version, String country) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(OEM_ID).is(oemId);
        criteria.and(AccountingOemFsCellCode.YEAR).is(year);
        criteria.and(AccountingOemFsCellCode.VERSION).is(version);
        criteria.and(COUNTRY).is(country);
        return this.getMongoTemplate().find(Query.query(criteria), AccountingOemFsCellCode.class);
    }

    @Override
    public AccountingOemFsCellCode findByCodeOemIdYearAndCountry(String fsCellCode, Integer year, String oemId, String country) {
        Criteria criteria = this.criteriaForNonDeleted();
        criteria.and(AccountingOemFsCellCode.CODE).is(fsCellCode);
        criteria.and(AccountingOemFsCellCode.YEAR).is(year);
        criteria.and(OEM_ID).is(oemId);
        criteria.and(COUNTRY).is(country);
        return this.getMongoTemplate().findOne(Query.query(criteria), this.getBeanClass());
    }

    @Override
    public List<AccountingOemFsCellCode> findByCodesAndDealerIdAndOemIdNonDeleted(List<String> fsCellCodes, Integer year, String oemId, String country) {
        Criteria criteria = this.criteriaForNonDeleted();
        criteria.and(AccountingOemFsCellCode.CODE).in(fsCellCodes);
        criteria.and(AccountingOemFsCellCode.YEAR).is(year);
        criteria.and(OEM_ID).is(oemId);
        criteria.and(COUNTRY).is(country);
        return this.getMongoTemplate().find(Query.query(criteria), this.getBeanClass());
    }

    @Override
    public List<BulkWriteUpsert> updateBulk(List<AccountingOemFsCellCode> fsCellCodes) {
        if(TCollectionUtils.isEmpty(fsCellCodes)){
            return null;
        }
        BulkOperations bulkOperations = getMongoTemplate().bulkOps(BulkOperations.BulkMode.ORDERED, AccountingOemFsCellCode.class);
        fsCellCodes.forEach(fsCellCode -> {
            Query query = Query.query(Criteria.where(TConstants.ID).in(fsCellCode.getId()));
            if (TStringUtils.isBlank(fsCellCode.getId())) {
                query = Query.query(Criteria.where(TConstants.ID).in(new ObjectId().toHexString()));
            }
            BasicDBObject dbDoc = new BasicDBObject();
            getMongoTemplate().getConverter().write(fsCellCode, dbDoc);
            Update update = TMongoUtils.fromDBObjectExcludeNullFields(dbDoc, Sets.newHashSet("_id", "id"));
            bulkOperations.upsert(query, update);
        });
        return bulkOperations.execute().getUpserts();
    }

    @Override
    public List<BulkWriteUpsert> updateBulkOemCode(List<AccountingOemFsCellCode> fsCellCodes) {
        if(TCollectionUtils.isEmpty(fsCellCodes)){
            return null;
        }
        BulkOperations bulkOperations = getMongoTemplate().bulkOps(BulkOperations.BulkMode.ORDERED, AccountingOemFsCellCode.class);
        long time = System.currentTimeMillis();
        fsCellCodes.forEach(fsCellCode -> {
            if( Objects.isNull(fsCellCode.getId())){
                return;
            }
            Query query = Query.query(Criteria.where(TConstants.ID).in(fsCellCode.getId()));
            Update update = new Update();
            update.set(AccountingOemFsCellCode.OEM_CODE,fsCellCode.getOemCode());
            update.set(AccountingOemFsCellCode.DURATION_TYPE,fsCellCode.getDurationType());
            update.set(MODIFIED_TIME,time);
            bulkOperations.upsert(query, update);
        });
        return bulkOperations.execute().getUpserts();
    }

    @Override
    public List<BulkWriteUpsert> delete(List<AccountingOemFsCellCode> fsCellCodes) {
        if(TCollectionUtils.isEmpty(fsCellCodes)){
            return null;
        }
        BulkOperations bulkOperations = getMongoTemplate().bulkOps(BulkOperations.BulkMode.ORDERED, AccountingOemFsCellCode.class);
        long time = System.currentTimeMillis();
        fsCellCodes.forEach(fsCellCode -> {
            if( Objects.isNull(fsCellCode.getId())){
                return;
            }

            Query query = Query.query(Criteria.where(TConstants.ID).in(fsCellCode.getId()));
            Update update = new Update();

            update.set(AccountingOemFsCellCode.CODE, getTimeStampAppended(fsCellCode.getCode()));
            update.set(TConstants.DELETED, true);
            update.set(MODIFIED_TIME, time);
            bulkOperations.upsert(query, update);
        });
        return bulkOperations.execute().getUpserts();
    }

    @Override
    public void addCountryInOemFsCellCodes() {
        Criteria criteria = criteriaForNonDeleted();
        Query q = new Query(criteria);
        Update update = new Update();
        update.set(COUNTRY, COUNTRY_US);
        update.set(MODIFIED_TIME, System.currentTimeMillis());
        update.set(MODIFIED_BY_USER_ID, UserContextProvider.getCurrentUserId());
        getMongoTemplate().updateMulti(q, update, AccountingOemFsCellCode.class);
    }

    @Override
    public void remove(String oem, Integer year, String country){
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(COUNTRY).is(country);
        criteria.and(YEAR).is(year);
        criteria.and(OEM_ID).is(oem);

        getMongoTemplate().remove(Query.query(criteria), this.getBeanClass());
    }

    private String getTimeStampAppended(String code){
        return code+"_"+System.currentTimeMillis();
    }

}
