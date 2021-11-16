package com.tekion.accounting.fs.common.enums;

import lombok.Getter;

@Getter
public enum  Template {
	POWER_POSTING("Power posting", "Power_posting.xlsx");

	private Template(String displayName, String fileName) {
		this.displayName = displayName;
		this.fileName = fileName;
	}

	private String displayName;
	private String fileName;
}
