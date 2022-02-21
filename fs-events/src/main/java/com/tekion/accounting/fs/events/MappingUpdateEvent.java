package com.tekion.accounting.fs.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.tekion.core.messaging.kafka.EventDestinationConfig;
import com.tekion.core.messaging.kafka.events.TContextAwareEvent;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@EventDestinationConfig(topics = {"financial_statements_events"})
@Accessors(chain =true)
public class MappingUpdateEvent extends TContextAwareEvent {

	public static String TYPE = "fsMappingUpdates";
	public MappingUpdateEvent(){
		setTypeName(TYPE);
	}

	String oemId;
	String groupCode;
	String fsId;
	Set<String> prevGlAccounts;
	Set<String> currentGlAccounts;
}
