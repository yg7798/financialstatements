package com.tekion.accounting.fs;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tekion.accounting.fs.utils.UserContextUtils;
import com.tekion.admin.beans.dealersetting.DealerMaster;
import com.tekion.admin.beans.property.DealerProperty;
import com.tekion.client.globalsettings.GlobalSettingsClient;
import com.tekion.clients.dealerproperty.DealerPropertyService;
import com.tekion.clients.preference.client.PreferenceClient;
import com.tekion.clients.preference.client.PreferenceClientFactory;
import com.tekion.core.feign.ClientBuilder;
import com.tekion.core.service.internalauth.AbstractServiceClientFactory;
import com.tekion.core.service.internalauth.TokenGenerator;
import com.tekion.core.utils.async.DynamicScalingExecutorService;
import com.tekion.core.utils.async.ScalingThreadPoolExecutor;
import com.tekion.core.utils.springasync.DelegatingUserContextExecutorServiceToAsyncTaskWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.tekion.core.utils.TGlobalConstants.SERVICE_NAME;

@Configuration
@Slf4j
public class BeanFactory {

	@Bean
	public GlobalSettingsClient globalSettingsClient(ClientBuilder builder, AbstractServiceClientFactory abstractServiceClientFactory, TokenGenerator generator) {
		return GlobalSettingsClient.createClient(builder, abstractServiceClientFactory, generator);
	}

	@Bean
	public PreferenceClient getPreferenceClient(PreferenceClientFactory preferenceClientFactory) {
		return preferenceClientFactory.createClient();
	}

	@Bean(name = "dealerCache")
	public LoadingCache<String, DealerMaster> dealerMasterCache(PreferenceClient preferenceClient, DealerPropertyService dealerPropertyService) {
		return CacheBuilder.newBuilder()
				.expireAfterWrite(1, TimeUnit.HOURS)
				.build(
						new CacheLoader<String, DealerMaster>() {
							@Override
							public DealerMaster load(String key) {
								DealerProperty dealerProperty = dealerPropertyService.getCachedStore().getPropertyForCurrentDealer(TConstants.ACCOUNTING_SITE_OVERRIDE_ENABLED);
								DealerMaster dealerMaster;
								if (Objects.nonNull(dealerProperty) && (Boolean) dealerProperty.getValue()) {
									dealerMaster = preferenceClient.fetchOemSiteDealerMaster(UserContextUtils.getSiteIdFromUserContext()).getData();
								} else {
									dealerMaster = preferenceClient.fetchOemSiteDealerMaster(UserContextUtils.getDefaultSiteId()).getData();
								}
								//log.info("dealerMaster for site id {} : {}",UserContextUtils.getSiteIdFromUserContext(), JsonUtil.toJson(dealerMaster));
								return dealerMaster;
							}
						});
	}


	@Bean(name = AsyncContextDecorator.ASYNC_THREAD_POOL)
	public AsyncTaskExecutor asyncExecutor(DynamicScalingExecutorService factory) {
		final ScalingThreadPoolExecutor pool = factory.createScaleFirstInifiniteQueueExecutor(
				SERVICE_NAME + "asyncThreadPool-",
				10,
				200,
				1,
				TimeUnit.MINUTES,
				new ThreadPoolExecutor.CallerRunsPolicy());
		return new DelegatingUserContextExecutorServiceToAsyncTaskWrapper(pool);
	}

	@Bean
	@Primary
	@DependsOn(value = "uriBuilderFactory")
	public RestTemplate externalRestTemplate(RestTemplateBuilder builder) {
		RestTemplate restTemplate = builder.build();
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
		messageConverters.add(converter);
		restTemplate.setMessageConverters(messageConverters);
		restTemplate.setUriTemplateHandler(uriBuilderFactory());
		return restTemplate;
	}

	@Bean
	public DefaultUriBuilderFactory uriBuilderFactory() {
		return new DefaultUriBuilderFactory(System.getenv("config_host"));
	}
}
