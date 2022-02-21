package com.tekion.accounting.fs.service.eventing.producers;

import com.tekion.accounting.fs.common.utils.JsonUtil;
import com.tekion.accounting.fs.events.MappingUpdateEvent;
import com.tekion.accounting.fs.events.enums.FSTopic;
import com.tekion.core.messaging.EventDispatcher;
import com.tekion.core.messaging.kafka.events.TBaseEvent;
import com.tekion.core.messaging.kafka.events.TContextAwareEvent;
import com.tekion.core.utils.UserContextAwareCallable;
import com.tekion.core.utils.UserContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An kafka event helper bean. Dispatches events to kafka.
 */
@Component
@Slf4j
public class FSEventHelper {
	private final ExecutorService executorService = Executors.newFixedThreadPool(5);

	@Autowired
	private EventDispatcher eventDispatcher;

	public void dispatchEventForMappingUpdate(MappingUpdateEvent event){
		event.setKey(UserContextProvider.getCurrentDealerId());
		log.info("MappingUpdateEvent being dispatched from producer.Event details: {}", JsonUtil.toJson(event));
		setUserContextDetailsInKafkaEvent(event);
		doDispatchEvent(event);
	}

	private <T extends TContextAwareEvent> void setUserContextDetailsInKafkaEvent(T event) {
		event.setDealerId(UserContextProvider.getCurrentDealerId());
		event.setTenantId(UserContextProvider.getCurrentTenantId());
		event.setUserId(UserContextProvider.getCurrentUserId());
		event.setForwardedHeaderMap(UserContextProvider.getForwardedHeaderMap());
	}

	private void doDispatchEvent(TBaseEvent event){
		try{
			UserContextAwareCallable<Object> task = new UserContextAwareCallable<Object>() {

				@Override
				public Object doCall() throws Exception {
					try{
						eventDispatcher.dispatchEventToMultipleTopics(event, FSTopic.FINANCIAL_STATEMENTS_EVENTS.getTopic());
					}catch (Exception e){
						log.error("Error while sending to kafka, ",e);
					}
					return null;
				}
			};
			executorService.submit(task);
		}catch (Exception e){
			log.error("Error while dispatching event",e);
		}
	}
}
