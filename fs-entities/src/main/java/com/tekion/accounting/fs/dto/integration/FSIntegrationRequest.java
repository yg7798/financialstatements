package com.tekion.accounting.fs.dto.integration;

import com.tekion.accounting.fs.integration.FinancialStatement;
import lombok.Data;

import java.util.List;

@Data
public class FSIntegrationRequest {
	List<FinancialStatement> financialStatements;
}
