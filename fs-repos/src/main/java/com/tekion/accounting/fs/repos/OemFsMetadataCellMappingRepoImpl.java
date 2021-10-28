package com.tekion.accounting.fs.repos;

import com.mongodb.client.result.UpdateResult;
import com.tekion.accounting.fs.master.beans.OemFSMetadataCellsInfo;
import com.tekion.accounting.fs.utils.JsonUtil;
import com.tekion.core.mongo.BaseGlobalMongoRepository;
import com.tekion.core.serverconfig.beans.ModuleName;
import com.tekion.core.utils.UserContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.tekion.accounting.fs.TConstants.*;

@Component
@Slf4j
public class OemFsMetadataCellMappingRepoImpl extends BaseGlobalMongoRepository<OemFSMetadataCellsInfo> implements OemFsMetadataCellMappingRepo {

    public OemFsMetadataCellMappingRepoImpl() {
        super(ModuleName.UNIVERSAL_GLOBAL.name(), OemFSMetadataCellsInfo.class);
    }

    @Override
    public OemFSMetadataCellsInfo getOemFsMetadataCellMapping(String oemId, Integer year, String version, String country) {
        Criteria criteria = getCriteriaForOemYearVersion(oemId, year, version, country);
        return this.getMongoTemplate().findOne(Query.query(criteria), OemFSMetadataCellsInfo.class);
    }

    @Override
    public List<OemFSMetadataCellsInfo> getOemFsMetadataCellMappingForAllYears(String oemId, String country) {
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(OemFSMetadataCellsInfo.OEM_ID).is(oemId);
        criteria.and(COUNTRY).is(country);
        return this.getMongoTemplate().find(Query.query(criteria), OemFSMetadataCellsInfo.class);
    }

    @Override
    public void delete(String oemId, Integer year, String version, String country) {
        Criteria criteria = getCriteriaForOemYearVersion(oemId, year, version, country);
        Query q = Query.query(criteria);

        Update updateQ = new Update();
        updateQ.set(DELETED, true);
        updateQ.set(MODIFIED_TIME, System.currentTimeMillis());

        UpdateResult updateResult = this.getMongoTemplate().updateMulti(q, updateQ, OemFSMetadataCellsInfo.class);
        log.info("OemFsMetadata soft deleted Records {}", JsonUtil.toJson(updateResult));
    }

    @Override
    public void update(OemFSMetadataCellsInfo updateDto) {
        Criteria criteria = getCriteriaForOemYearVersion(updateDto.getOemId(),updateDto.getYear(),updateDto.getVersion(), updateDto.getCountry());
        Query q = Query.query(criteria);
        Update updateQ = new Update();
        updateQ.set("cellMapping", updateDto.getCellMapping());
        updateQ.set(MODIFIED_TIME, System.currentTimeMillis());
        UpdateResult updateResult = this.getMongoTemplate().updateMulti(q, updateQ, OemFSMetadataCellsInfo.class);
        log.info("OemFsMetadata updated records {}", JsonUtil.toJson(updateResult));
    }

    private Criteria getCriteriaForOemYearVersion(String oemId, Integer year, String version, String country){
        Criteria criteria = criteriaForNonDeleted();
        criteria.and(OemFSMetadataCellsInfo.OEM_ID).is(oemId);
        criteria.and(OemFSMetadataCellsInfo.YEAR).is(year);
        criteria.and(OemFSMetadataCellsInfo.VERSION).is(version);
        criteria.and(COUNTRY).is(country);
        return criteria;
    }

    @Override
    public void addCountryInOemFsMetaDataMappings() {
        Criteria criteria = criteriaForNonDeleted();
        Query q = new Query(criteria);
        Update update = new Update();
        update.set(COUNTRY, COUNTRY_US);
        update.set(MODIFIED_TIME, System.currentTimeMillis());
        update.set(MODIFIED_BY_USER_ID, UserContextProvider.getCurrentUserId());
        getMongoTemplate().updateMulti(q, update, OemFSMetadataCellsInfo.class);
    }

}
