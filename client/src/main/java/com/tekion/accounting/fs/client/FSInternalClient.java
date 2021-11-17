package com.tekion.accounting.fs.client;

import com.tekion.core.feign.ClientBuilder;
import com.tekion.core.service.internalauth.AbstractServiceClientFactory;
import com.tekion.core.service.internalauth.FeignAuthInterceptor;
import com.tekion.core.service.internalauth.TokenGenerator;
import feign.Logger;
import feign.RequestInterceptor;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

public interface FSInternalClient {

	static FSInternalClient createClient(ClientBuilder builder, AbstractServiceClientFactory clientFactory, TokenGenerator generator ) {
		String csInstanceUrl = clientFactory.getServiceBaseUrl( "FINANCIAL-STATEMENTS");
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
}
