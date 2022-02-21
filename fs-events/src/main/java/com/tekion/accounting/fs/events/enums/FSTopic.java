package com.tekion.accounting.fs.events.enums;

public enum FSTopic {
	FINANCIAL_STATEMENTS_EVENTS("financial_statements_events");

	private final String topic;

	FSTopic(String topic) {
		this.topic = topic;
	}

	public String getTopic(){
		return topic;
	}
}
