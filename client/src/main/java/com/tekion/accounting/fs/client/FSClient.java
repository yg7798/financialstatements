package com.tekion.accounting.fs.client;

import com.tekion.accounting.fs.entities.AccountingInfo;
import com.tekion.accounting.fs.entities.OEMFsCellCodeSnapshotBulkResponseDto;
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

}

