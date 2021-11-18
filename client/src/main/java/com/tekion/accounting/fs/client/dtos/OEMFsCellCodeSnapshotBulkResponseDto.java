package com.tekion.accounting.fs.client.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OEMFsCellCodeSnapshotBulkResponseDto {
	private Long timestamp;
	private String code;
	private BigDecimal value;
}
