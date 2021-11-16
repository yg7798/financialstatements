package com.tekion.accounting.fs.service.compute.models;

import lombok.Data;

import java.util.Set;

@Data
public class FSReportDto {
	private Long fromTimestamp;
	private Long toTimestamp;
	Set<String> codes;
}