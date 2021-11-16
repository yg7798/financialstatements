package com.tekion.accounting.fs.repos.accountingInfo;

import com.tekion.accounting.fs.beans.accountingInfo.AccountingInfo;

import java.util.Collection;
import java.util.List;

public interface AccountingInfoRepo {
	AccountingInfo findByDealerIdNonDeleted(String DealerId);

	List<AccountingInfo> findByDealerIdNonDeleted(Collection<String> DealerIds);

	AccountingInfo save(AccountingInfo info);
}
