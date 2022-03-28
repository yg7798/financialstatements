package com.tekion.accounting.fs.repos.fsValidation;

import com.google.common.collect.Sets;
import com.mongodb.BasicDBObject;
import com.mongodb.bulk.BulkWriteResult;
import com.tekion.accounting.fs.beans.fsValidation.FsValidationRule;
import com.tekion.accounting.fs.common.utils.TMongoUtils;
import com.tekion.core.mongo.BaseGlobalMongoRepository;
import com.tekion.core.serverconfig.beans.ModuleName;
import com.tekion.core.utils.TCollectionUtils;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

import static com.tekion.accounting.fs.beans.common.AccountingOemFsCellCode.OEM_CODE;
import static com.tekion.accounting.fs.beans.common.AccountingOemFsCellCode.OEM_ID;
import static com.tekion.accounting.fs.beans.fsValidation.FsValidationRule.ERROR_CODE;
import static com.tekion.accounting.fs.common.TConstants.*;

@Component
public class FsValidationRepoImpl extends BaseGlobalMongoRepository<FsValidationRule> implements FsValidationRepo {

	public FsValidationRepoImpl() {
		super(ModuleName.UNIVERSAL_GLOBAL.name(), FsValidationRule.class);
	}

	@Override
	public List<FsValidationRule> getValidationRules(String oemId, Collection<Integer> years, String country) {
		Criteria criteria = criteriaForNonDeleted();
		criteria.and(COUNTRY).is(country);
		criteria.and(YEAR).in(years);
		criteria.and(OEM_ID).is(oemId);

		return getMongoTemplate().find(Query.query(criteria), this.getBeanClass());
	}

	@Override
	public BulkWriteResult bulkUpsert(List<FsValidationRule> rules) {
		if (TCollectionUtils.isEmpty(rules)) {
			return null;
		}
		BulkOperations bulkOperations = getMongoTemplate().bulkOps(BulkOperations.BulkMode.UNORDERED, FsValidationRule.class);
		rules.forEach(rule -> {
			Query query = Query.query(
					Criteria.where(OEM_ID).is(rule.getOemId()).
							and(YEAR).is(rule.getYear()).
							and(COUNTRY).is(rule.getCountry()).
							and(OEM_CODE).is(rule.getOemCode()).
							and(ERROR_CODE).is(rule.getErrorCode())
			);
			BasicDBObject dbDoc = new BasicDBObject();
			getMongoTemplate().getConverter().write(rule, dbDoc);
			Update update = TMongoUtils.fromDBObjectExcludeNullFields(dbDoc, Sets.newHashSet("_id", "id"));
			bulkOperations.upsert(query, update);
		});
		return bulkOperations.execute();
	}

	@Override
	public void remove(String oemId, Integer year, String country) {
		Criteria criteria = criteriaForNonDeleted();
		criteria.and(COUNTRY).is(country);
		criteria.and(YEAR).is(year);
		criteria.and(OEM_ID).is(oemId);

		getMongoTemplate().remove(Query.query(criteria), this.getBeanClass());
	}

	@Override
	public void delete(Collection<String> ids) {
		Criteria criteria = criteriaForNonDeleted();
		criteria.and(ID).in(ids);

		Update update = new Update();
		update.set(COUNTRY, ""+System.currentTimeMillis());
		update.set(DELETED, true);

		getMongoTemplate().findAndModify(Query.query(criteria), update, this.getBeanClass());
	}
}
