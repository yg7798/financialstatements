package com.tekion.accounting.fs.enums;

public enum FSType {
	OEM("OEM"),
	INTERNAL("Internal"),
	CONSOLIDATED ("Consolidated");

	private final String displayName;

	FSType(String displayName){
		this.displayName = displayName;
	}

	public String getDisplayName(){
		return this.displayName;
	}
}
