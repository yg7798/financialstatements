package com.tekion.accounting.fs.service.eventing.configs;

import com.google.common.collect.Maps;
import com.tekion.core.messaging.kafka.config.AbstractKafkaConsumerConfig;
import com.tekion.core.messaging.kafka.events.eventListener.EventListenerFactory;
import com.tekion.core.serverconfig.service.ServerConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

/**
 * Kafka consumer config class.
 * Import the configuration when required in the service.
 * To support more topics, add the topics to suppotedTopics bean.
 */
@Slf4j
@Profile("!dev")
public class FSKafkaConsumerConfig extends AbstractKafkaConsumerConfig {

	private String ACCOUNTING_SLOW_TOPIC = "accounting_slow_events";

	/**
	 * Max records consumer will poll in one go. Max time to process is 300 sec (5 mins), gives 60 secs for an event to be processed.
	 */
	private static final int MAX_POLL_RECORDS = 5;

	@Autowired
	public FSKafkaConsumerConfig(ServerConfigService configService, EventListenerFactory eventListenerFactory, @Qualifier("defaultProducerFactory") ProducerFactory producerFactory) {
		super(configService, eventListenerFactory, producerFactory);
	}

	/**
	 * @return An array of supported topics prefixed by cluster type.
	 */
	@Bean
	@Override
	public String[] supportedTopics() {
		//String clusterType = System.getenv("CLUSTER_TYPE");
		return new String[]{ ACCOUNTING_SLOW_TOPIC };
	}

	@Override
	public String groupId() {
		return "financial-statements-consumers";
	}

	protected Map<String, Object> getConsumerProps() {
		Map<String, Object> consumerProps = Maps.newHashMap();
		// consumerProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 30*1000);
		consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, MAX_POLL_RECORDS);
		return consumerProps;
	}
}
