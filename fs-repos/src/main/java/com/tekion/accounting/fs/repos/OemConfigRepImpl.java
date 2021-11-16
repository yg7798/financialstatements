package com.tekion.accounting.fs.repos;


import com.tekion.accounting.fs.beans.common.OemConfig;
import com.tekion.core.mongo.BaseGlobalMongoRepository;
import com.tekion.core.serverconfig.beans.ModuleName;
import com.tekion.core.utils.UserContextProvider;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import static com.tekion.accounting.fs.common.TConstants.*;


@Component
public class OemConfigRepImpl extends BaseGlobalMongoRepository<OemConfig> implements OemConfigRepo {

    public OemConfigRepImpl() {
        super(ModuleName.UNIVERSAL_GLOBAL.name(), OemConfig.class);
    }

    @Override
    public OemConfig save(OemConfig oemConfig) {
        return getMongoTemplate().save(oemConfig);
    }

    @Override
    public OemConfig findByOemId(String oemId, String country) {
        Criteria criteria = criteriaForNonDeleted();
        //criteria.and(TConstants.DEALER_ID).is(dealerId);
        criteria.and(OemConfig.OEM_ID).is(oemId);
        criteria.and(COUNTRY).is(country);
        return getMongoTemplate().findOne(Query.query(criteria), OemConfig.class);
    }

    @Override
    public void addCountryInOemConfigs() {
        Criteria criteria = criteriaForNonDeleted();
        Query q = new Query(criteria);
        Update update = new Update();
        update.set(COUNTRY, COUNTRY_US);
        update.set(MODIFIED_TIME, System.currentTimeMillis());
        update.set(MODIFIED_BY_USER_ID, UserContextProvider.getCurrentUserId());
        getMongoTemplate().updateMulti(q, update, OemConfig.class);
    }
}
