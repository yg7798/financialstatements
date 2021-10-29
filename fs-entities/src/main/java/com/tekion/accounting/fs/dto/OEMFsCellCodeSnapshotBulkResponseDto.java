package com.tekion.accounting.fs.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OEMFsCellCodeSnapshotBulkResponseDto extends OEMFsCellCodeSnapshotResponseDto {
    private Long timestamp;

    public OEMFsCellCodeSnapshotBulkResponseDto(String code, BigDecimal value, Long timestamp) {
        super(code, value);
        this.timestamp = timestamp;
    }
}
