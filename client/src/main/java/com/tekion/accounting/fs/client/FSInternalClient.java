package com.tekion.accounting.fs.client;

import com.tekion.accounting.fs.client.dtos.AccountingInfo;
import com.tekion.accounting.fs.client.dtos.OEMFsCellCodeSnapshotBulkResponseDto;
import com.tekion.core.beans.TResponse;
import com.tekion.core.feign.ClientBuilder;
import com.tekion.core.service.internalauth.AbstractServiceClientFactory;
import com.tekion.core.service.internalauth.FeignAuthInterceptor;
import com.tekion.core.service.internalauth.TokenGenerator;
import feign.*;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FSInternalClient {

	static FSInternalClient createClient(ClientBuilder builder, AbstractServiceClientFactory clientFactory, TokenGenerator generator ) {
		String csInstanceUrl = clientFactory.getServiceBaseUrl( "FINANCIAL_STATEMENTS");
		return createClient( builder, csInstanceUrl, generator );
	}

	static FSInternalClient createClient(
			@NotNull ClientBuilder builder, @NotNull String hostUrl, @NotNull TokenGenerator generator ) {
		List<RequestInterceptor> interceptors = Collections.singletonList( new FeignAuthInterceptor( generator ) );
		Logger.Level level = Logger.Level.FULL;
		if (System.getenv("CLUSTER_TYPE").toLowerCase().contains("prod")) {
			level = Logger.Level.BASIC;
		}
		return builder.buildClient(
				hostUrl, FSInternalClient.class, level, new FSClientErrorDecoder(), null, interceptors );
	}

	@RequestLine("POST financial-statements/u/oemMapping/fsReport/{oemId}/{oemFsYear}/v/{oemFsVersion}/bulk")
	TResponse<List<OEMFsCellCodeSnapshotBulkResponseDto>> getFSReportBulk(@HeaderMap Map<String, String> headerMap,
																		  Set<String> codes,
																		  @Param("oemId") String oemId,
																		  @Param("oemFsVersion") Integer oemFsVersion,
																		  @Param("oemFsYear") Integer oemFsYear,
																		  @QueryMap Map<String,String> queryMap);

	@RequestLine("GET financial-statements/u/accountingInfo/")
	TResponse<AccountingInfo> getAccountingInfo(@HeaderMap Map<String, String> headerMap);
}
