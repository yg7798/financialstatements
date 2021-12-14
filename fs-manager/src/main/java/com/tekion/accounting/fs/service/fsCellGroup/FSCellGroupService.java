package com.tekion.accounting.fs.service.fsCellGroup;

import com.tekion.accounting.fs.beans.common.AccountingOemFsCellGroup;

import java.util.List;

public interface FSCellGroupService {
	List<AccountingOemFsCellGroup> findGroupCodes(List<String> groupCodes, String oemId, Integer year, Integer version);
}
