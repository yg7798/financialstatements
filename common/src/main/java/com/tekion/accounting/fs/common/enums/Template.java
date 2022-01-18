package com.tekion.accounting.fs.common.enums;

import lombok.Getter;

@Getter
public enum  Template {
	PCL_CODES_UPDATE("Pcl code bulk update", "pclCodes_update.xlsx");

	private Template(String displayName, String fileName) {
		this.displayName = displayName;
		this.fileName = fileName;
	}

	private String displayName;
	private String fileName;
}
