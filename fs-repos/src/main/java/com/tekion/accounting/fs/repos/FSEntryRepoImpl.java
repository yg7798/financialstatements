package com.tekion.accounting.fs.repos;

import com.tekion.accounting.fs.TConstants;
import com.tekion.accounting.fs.master.beans.FSEntry;
import com.tekion.core.mongo.BaseDealerLevelMongoRepository;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.tekion.accounting.fs.TConstants.*;

@Component
public class FSEntryRepoImpl extends BaseDealerLevelMongoRepository<FSEntry>
    implements FSEntryRepo {

  public FSEntryRepoImpl() {
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
  public FSEntry findByIdAndDealerId(String id, String dealerId) {
    Criteria criteria = this.criteriaForNonDeleted();
    criteria.and(ID).is(id);
    criteria.and(TConstants.DEALER_ID).is(dealerId);
    return this.findOne(criteria, this.getBeanClass(), this.getMongoTemplate());
  }
}
