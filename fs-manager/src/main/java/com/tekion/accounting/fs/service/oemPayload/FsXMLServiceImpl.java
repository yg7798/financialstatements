package com.tekion.accounting.fs.service.oemPayload;

import com.tekion.accounting.fs.dto.request.FinancialReportRequestBody;
import com.tekion.accounting.fs.dto.request.FinancialStatXmlRequest;
import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.core.utils.UserContextProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
@Slf4j
public class FsXMLServiceImpl {

	@Autowired
	@Qualifier("noFrillsRestTemplate")
	private final RestTemplate restTemplate;
	private final DealerConfig dealerConfig;


	private static String FS_BUILDER_URL;
	private static String API_KEY;
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String X_API_KEY_HEADER = "x-api-key";


	@PostConstruct
	public void postContruct() {
		String clusterType = System.getenv("CLUSTER_TYPE").toLowerCase();
		log.info("Cluster type {} ", clusterType);
		if (clusterType.contains("tst")) {
			FS_BUILDER_URL = "https://0n6vfbrmye.execute-api.us-west-1.amazonaws.com/tst/dms-gm-fs-builder";
			API_KEY = "XekARAX6qX9RHzJGCeygi47Ib6QWE4ne5x8JROmY";
		} else if (clusterType.contains("stage")) {
			FS_BUILDER_URL = "https://7cgpscryva.execute-api.us-west-1.amazonaws.com/dev/gmfs";
			API_KEY = "uWnGqp8IIY3HJF0998C0A1zbwbf5NeET9zRfnF4z";
		} else {
			FS_BUILDER_URL = "https://d7zajspxqg.execute-api.us-west-1.amazonaws.com/dmsapi/gmfs";
			API_KEY = "Y5GH4jS0Kq8FjIFmFhW9r42BV065fU0X75Ehv2oc";
		}
		log.info("FS URL {} {} ", FS_BUILDER_URL, API_KEY);
	}

	public String getFinancialStatement(FinancialReportRequestBody reqBody) {
		String year = reqBody.getYear();
		String month = reqBody.getMonth();
		if (month != null) {
			month = (Integer.parseInt(month) + 1) + "";
		}
		FinancialStatXmlRequest request = FinancialStatXmlRequest.builder()
				.dealerId(UserContextProvider.getCurrentDealerId())
				.dealerNumber(dealerConfig.getDealerMaster().getOemDealerId())
				.tenantName(getTenantName())
				.year(year)
				.month(month).build();
		log.info("Calling FS builder with tenant: {}, Req: {} ", UserContextProvider.getCurrentTenantId(), request);
		HttpEntity<FinancialStatXmlRequest> httpEntity = new HttpEntity<>(request, buildHeaders());
		ResponseEntity<String> response = restTemplate.exchange(FS_BUILDER_URL, HttpMethod.POST, httpEntity, String.class);
		return response.getBody();
	}

	private String getTenantName() {
		return isStage() ? "stg-" + UserContextProvider.getCurrentTenantId() : UserContextProvider.getCurrentTenantId();
	}

	public static HttpHeaders buildHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.set(X_API_KEY_HEADER, API_KEY);
		headers.set(CONTENT_TYPE, "application/json");
		return headers;
	}

	private boolean isStage() {
		return "stage_cloud".equals(System.getenv("CLUSTER_TYPE"));
	}
}

