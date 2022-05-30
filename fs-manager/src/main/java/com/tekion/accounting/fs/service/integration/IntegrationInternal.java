package com.tekion.accounting.fs.service.integration;

import com.tekion.accounting.fs.dto.integration.FSIntegrationRequest;
import com.tekion.accounting.fs.dto.integration.FSSubmitResponse;
import feign.HeaderMap;
import feign.RequestLine;

import java.util.Map;

public interface IntegrationInternal {
	@RequestLine("POST /realtime/integration/u/v1/ProcessFinancialStatement")
	FSSubmitResponse submitFS(@HeaderMap Map<String, String> headerMap, FSIntegrationRequest fsRequest);

	@RequestLine("POST /realtime/integration/u/v1/DownloadFinancialStatement")
	FSSubmitResponse downloadFs(@HeaderMap Map<String, String> headerMap, FSIntegrationRequest fsRequest);
}
