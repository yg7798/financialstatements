package com.tekion.accounting.fs.client;


import com.tekion.accounting.fs.client.dtos.*;
import com.tekion.core.beans.TResponse;

import java.util.List;
import java.util.Set;

public interface FSClient {

	TResponse<List<OEMFsCellCodeSnapshotBulkResponseDto>> getFSReportBulk(Set<String> codes,
																		  Long fromTimestamp,
																		  Long toTimestamp,
																		  boolean includeM13,
																		  String oemId,
																		  Integer oemFsVersion,
																		  Integer oemFsYear);

	TResponse<AccountingInfo> getAccountingInfo();

	TResponse<List<FsEntryDto>> getFsEntries();

	TResponse<List<CellGroupDto>> getCellGroups(String oemId, Integer year, Integer version, Set<String> groupCodes);

	TResponse<List<FsMappingDto>> getFsMappings(String fsId, Set<String> glAccountIds);
}

