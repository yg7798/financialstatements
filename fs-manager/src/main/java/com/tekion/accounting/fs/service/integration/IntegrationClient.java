package com.tekion.accounting.fs.service.integration;

import com.tekion.accounting.fs.dto.integration.FSIntegrationRequest;
import com.tekion.accounting.fs.dto.integration.FSSubmitResponse;
import com.tekion.accounting.fs.enums.AccountingError;
import com.tekion.accounting.fs.service.oemPayload.OEMInfo;
import com.tekion.accounting.fs.common.utils.JsonUtil;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.feign.ClientBuilder;
import com.tekion.core.service.internalauth.AbstractServiceClientFactory;
import com.tekion.core.utils.TRequestUtils;
import feign.Logger;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class IntegrationClient {
	private IntegrationInternal integrationInternal;
	private final AbstractServiceClientFactory factory;
	private final ClientBuilder clientBuilder;

	private static final String OEM = "oem";
	private static final String BRAND = "brand";
	private static final String SITE = "tek-siteId";

	@PostConstruct
	public void init(){
		integrationInternal = this.getInternalClient();
	}

	public FSSubmitResponse submitFS(FSIntegrationRequest fsIntegrationRequest){
		FSSubmitResponse response = integrationInternal.submitFS(getHeaders(),fsIntegrationRequest);
		log.info("Response from integartion {} ",response);
		return response;
	}

	public FSSubmitResponse submitFS(FSIntegrationRequest fsIntegrationRequest, OEMInfo oemInfo, String siteId){
		FSSubmitResponse response = integrationInternal.submitFS(getHeaders(oemInfo, siteId), fsIntegrationRequest);
		log.info("Response from integration {} ",response);
		return response;
	}

	public Map<String, String> getHeaders(){
		Map<String, String> headers = TRequestUtils.userCallHeaderMap();
		return headers;
	}

	public Map<String, String> getHeaders(OEMInfo info, String fsSiteId){
		Map<String, String> headers = TRequestUtils.userCallHeaderMap();
		headers.put(OEM, info.getOem());
		headers.put(BRAND, info.getBrand());
		headers.put(SITE, fsSiteId);
		log.info("FsSubmit headers {}", JsonUtil.toJson(headers));
		return headers;

	}

	private IntegrationInternal getInternalClient() {
		String csInstanceUrl = factory.getServiceBaseUrl("TINPROXY");
		log.info("URL for integration {} {}",csInstanceUrl);
		return clientBuilder.buildClient(csInstanceUrl, IntegrationInternal.class, Logger.Level.BASIC,new ErrorDecoder() {
			@Override
			public Exception decode(String methodKey, Response response) {
				log.error("Error occurred while submitting the statement {} ",response.status(),response.reason());
				try{
					log.error("Error resposne {}", IOUtils.toString(response.body().asInputStream()));
				}catch (Exception e){}
				return new TBaseRuntimeException(AccountingError.fsSubmitError);
			}
		}, null,null);
//        return HystrixFeign.builder()
//                .encoder(new JacksonEncoder())
//                .decoder(new JacksonDecoder())
//                .logLevel(Logger.Level.FULL)
//                .errorDecoder(new ErrorDecoder() {
//                    @Override
//                    public Exception decode(String methodKey, Response response) {
//                        log.error("Error occurred while submitting the statement {} ",response.status(),response.reason());
//                        try{
//                            log.error("Error resposne {}",IOUtils.toString(response.body().asInputStream()));
//                        }catch (Exception e){}
//                        return new TBaseRuntimeException(AccountingError.fsSubmitError);
//                    }
//                })
//                .target(IntegrationInternal.class, csInstanceUrl);
	}
}

