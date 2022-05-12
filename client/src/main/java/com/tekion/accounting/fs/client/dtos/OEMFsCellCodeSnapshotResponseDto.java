package com.tekion.accounting.fs.client.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OEMFsCellCodeSnapshotResponseDto {
	private String code;
	private BigDecimal value;
}
