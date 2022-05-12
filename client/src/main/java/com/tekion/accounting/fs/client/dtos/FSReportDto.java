package com.tekion.accounting.fs.client.dtos;

import lombok.Data;

import java.util.Set;

@Data
public class FSReportDto {
	private Long fromTimestamp;
	private Long toTimestamp;
	Set<String> codes;
}
