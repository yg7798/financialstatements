package com.tekion.accounting.fs.service.accountingInfo;

import com.tekion.accounting.fs.beans.accountingInfo.AccountingInfo;
import com.tekion.accounting.fs.dto.accountingInfo.AccountingInfoDto;
import com.tekion.accounting.fs.enums.OEM;

import java.util.Collection;
import java.util.List;

public interface AccountingInfoService {

	AccountingInfo saveOrUpdate(AccountingInfoDto dto);

	AccountingInfo find(String dealerId);

	List<AccountingInfo> findList(Collection<String> dealerIds);

	AccountingInfo delete(String dealerId);

	AccountingInfo populateOEMFields();

	AccountingInfo addOem(OEM oem);

	AccountingInfo removeOem(OEM oem);

	AccountingInfo setPrimaryOem(OEM oem);

	void migrateFsRoundOffOffset();
}
