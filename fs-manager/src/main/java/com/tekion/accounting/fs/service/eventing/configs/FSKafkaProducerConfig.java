package com.tekion.accounting.fs.service.eventing.configs;

import com.tekion.core.messaging.kafka.config.AbstractKafkaProducerConfig;
import com.tekion.core.messaging.kafka.config.properties.KafkaProducerProperties;
import com.tekion.core.serverconfig.service.ServerConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * Represents the kafka producer configuration.
 */
@Slf4j
@Configuration
public class FSKafkaProducerConfig extends AbstractKafkaProducerConfig {

	public static final Integer REQUEST_TIMEOUT_MS_CONFIG = 10*1000; // 10 sec

	@Autowired
	public FSKafkaProducerConfig(ServerConfigService configService) {
		super(configService);
	}


	@Override
	protected KafkaProducerProperties producerProperties() {
		KafkaProducerProperties kafkaProducerProperties = super.producerProperties();
        //kafkaProducerProperties.setREQUEST_TIMEOUT_MS_CONFIG(REQUEST_TIMEOUT_MS_CONFIG);
		return kafkaProducerProperties;
	}
}
