package com.tekion.accounting.fs.service;

import com.tekion.accounting.fs.dto.FSViewStatementDto;

public interface FSPrintService {
	Object viewStatement(FSViewStatementDto dto);
}
