package com.tekion.accounting.fs.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tekion.accounting.fs.common.utils.UserContextUtils;
import com.tekion.admin.beans.property.DealerProperty;
import com.tekion.cachesupport.lib.cache.RedisCacheFactory;
import com.tekion.client.globalsettings.GlobalSettingsClient;
import com.tekion.clients.dealerproperty.DealerPropertyService;
import com.tekion.clients.dealerproperty.DealerPropertyStore;
import com.tekion.clients.preference.client.PreferenceClient;
import com.tekion.clients.preference.client.PreferenceClientFactory;
import com.tekion.core.feign.ClientBuilder;
import com.tekion.core.feign.FeignBuilderFactory;
import com.tekion.core.serverconfig.service.ServerConfigService;
import com.tekion.core.service.internalauth.AbstractServiceClientFactory;
import com.tekion.core.service.internalauth.TokenGenerator;
import com.tekion.core.utils.TGlobalConstants;
import com.tekion.core.utils.async.DynamicScalingExecutorService;
import com.tekion.core.utils.async.ScalingThreadPoolExecutor;
import com.tekion.core.utils.springasync.DelegatingUserContextExecutorServiceToAsyncTaskWrapper;
import com.tekion.dealersettings.client.DealerSettingsClientFactory;
import com.tekion.dealersettings.client.IDealerSettingsClient;
import com.tekion.dealersettings.dealermaster.beans.DealerMaster;
import com.tekion.media.library.MediaClient;
import com.tekion.notificationsv2.client.NotificationsV2Client;
import com.tekion.notificationsv2.client.NotificationsV2ClientFactory;
import com.tekion.printerclient.PrinterClient;
import com.tekion.printerclientv2.PrinterClientV2;
import com.tekion.sdk.storage.s3.S3ObjectStorageService;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.tekion.core.utils.TGlobalConstants.SERVICE_NAME;
import static feign.Logger.Level.FULL;

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

	@Bean
	public IDealerSettingsClient getDealerSettingsClient(DealerSettingsClientFactory dealerSettingsClientFactory) {
		return dealerSettingsClientFactory.createClient();
	}

	@Bean(name = "dealerCache")
	public LoadingCache<String, DealerMaster> dealerMasterCache(IDealerSettingsClient dealerSettingsClient, DealerPropertyService dealerPropertyService) {
		return CacheBuilder.newBuilder()
				.expireAfterWrite(5, TimeUnit.MINUTES)
				.build(
						new CacheLoader<String, DealerMaster>() {
							@Override
							public DealerMaster load(String key) {
								DealerProperty dealerProperty = dealerPropertyService.getCachedStore().getPropertyForCurrentDealer(TConstants.ACCOUNTING_SITE_OVERRIDE_ENABLED);
								DealerMaster dealerMaster;
								if (Objects.nonNull(dealerProperty) && (Boolean) dealerProperty.getValue()) {
									dealerMaster = dealerSettingsClient.fetchOemSiteDealerMaster(UserContextUtils.getSiteIdFromUserContext()).getData();
								} else {
									dealerMaster = dealerSettingsClient.fetchOemSiteDealerMaster(UserContextUtils.getDefaultSiteId()).getData();
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

	@Bean(value = "noFrillsRestTemplate")
	public RestTemplate otherRestTemplate() {
		return new RestTemplate();
	}

	@Bean
	public DefaultUriBuilderFactory uriBuilderFactory() {
		return new DefaultUriBuilderFactory(System.getenv("config_host"));
	}

	@Bean
	public ExecutorService executorService(DynamicScalingExecutorService factory) {
		return factory.createScaleFirstInifiniteQueueExecutor(
				SERVICE_NAME + "asyncThreadPool-",
				10,
				200,
				1,
				TimeUnit.MINUTES,
				new ThreadPoolExecutor.CallerRunsPolicy());
	}

	@Bean
	public S3ObjectStorageService getS3ObjectStorageService(ServerConfigService serverConfigService) {
		return new S3ObjectStorageService(serverConfigService);
	}

	@Bean
	@ConditionalOnMissingBean
	public RedisTemplate<String, Object> getGlobalRedisTemplate(@Autowired RedisCacheFactory redisCacheFactory) {
		return redisCacheFactory.getRedisTemplateForTenant(TGlobalConstants.NO_TENANT_ID);
	}

	@Bean
	public PrinterClientV2 printerClientV2(ClientBuilder clientBuilder) {
		return PrinterClientV2.createClient(clientBuilder);
	}

	@Bean
	public PrinterClient printerClient(FeignBuilderFactory feignBuilderFactory) {
		return feignBuilderFactory.buildFeignClientInstance(
				FeignBuilderFactory.buildInstanceUrl("pms"), PrinterClient.class, FULL, new ErrorDecoder.Default(), null);
	}

	@Bean
	public NotificationsV2Client createNotificationClient(NotificationsV2ClientFactory factory) {
		return factory.createClient();
	}

	@Bean
	public MediaClient getMediaClient(ClientBuilder builder, AbstractServiceClientFactory clientFactory, TokenGenerator generator, OkHttpClient okHttpClient, ObjectMapper mapper){
		return MediaClient.createClient(builder, clientFactory, generator, okHttpClient, mapper);
	}

	@Bean
	public DealerPropertyStore dealerPropertyStore(DealerPropertyService factory) {
		return factory.getDefaultStore();
	}
}
