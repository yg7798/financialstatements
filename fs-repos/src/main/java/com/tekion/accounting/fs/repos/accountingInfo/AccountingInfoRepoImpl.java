package com.tekion.accounting.fs.repos.accountingInfo;

import com.tekion.accounting.fs.beans.accountingInfo.AccountingInfo;
import com.tekion.core.mongo.BaseTenantLevelMongoRepository;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

import static com.tekion.accounting.fs.common.TConstants.DEALER_ID;
import static com.tekion.accounting.fs.common.TConstants.TENANT_DEFAULT;

@Component
public class AccountingInfoRepoImpl extends BaseTenantLevelMongoRepository<AccountingInfo> implements AccountingInfoRepo {
	public AccountingInfoRepoImpl() {
		super(AccountingInfo.class, TENANT_DEFAULT);
	}

	@Override
	public AccountingInfo findByDealerIdNonDeleted(String dealerId) {
		Criteria c = criteriaForNonDeleted();
		c.and(DEALER_ID).is(dealerId);
		return getMongoTemplate().findOne(Query.query(c), AccountingInfo.class);
	}

	@Override
	public List<AccountingInfo> findByDealerIdNonDeleted(Collection<String> dealerIds) {
		Criteria c = criteriaForNonDeleted();
		c.and(DEALER_ID).in(dealerIds);
		return getMongoTemplate().find(Query.query(c), AccountingInfo.class);
	}

	@Override
	public AccountingInfo save(AccountingInfo info) {
		return super.save(info);
	}
}
