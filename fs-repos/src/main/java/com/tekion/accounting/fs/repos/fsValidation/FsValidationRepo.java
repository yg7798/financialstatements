package com.tekion.accounting.fs.repos.fsValidation;

import com.mongodb.bulk.BulkWriteResult;
import com.tekion.accounting.fs.beans.fsValidation.FsValidationRule;

import java.util.Collection;
import java.util.List;

public interface FsValidationRepo {

	List<FsValidationRule> getValidationRules(String oemId, Collection<Integer> years, String country);

	BulkWriteResult bulkUpsert(List<FsValidationRule> rules);

	void remove(String oemId, Integer year, String country);

	void delete(Collection<String> ids);
}
