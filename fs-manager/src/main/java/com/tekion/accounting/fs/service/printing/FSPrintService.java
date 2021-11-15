package com.tekion.accounting.fs.service.printing;

import com.tekion.accounting.fs.service.printing.models.FSViewStatementDto;

public interface FSPrintService {
	Object viewStatement(FSViewStatementDto dto);
}
