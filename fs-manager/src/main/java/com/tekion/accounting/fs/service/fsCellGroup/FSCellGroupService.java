package com.tekion.accounting.fs.service.fsCellGroup;

import com.tekion.accounting.fs.beans.common.AccountingOemFsCellGroup;

import java.util.List;
import java.util.Set;

public interface FSCellGroupService {
	List<AccountingOemFsCellGroup> findGroupCodes(List<String> groupCodes, String oemId, Integer year, Integer version);

	void migrateCellGroupValuesForOem(String country, Integer fromYear, Integer toYear, Set<String> oemIds);
}
