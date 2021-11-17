package com.tekion.accounting.fs.service.compute.models;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CellCodeKey {
	String oemId;
	int year;
	int version;
	String countryCode;
}
