package com.tekion.accounting.fs.dto.cellcode;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OEMFsCellCodeSnapshotResponseDto {
    private String code;
    private BigDecimal value;

    public OEMFsCellCodeSnapshotResponseDto(String code, BigDecimal value) {
        this.code = code;
        this.value = value;
    }
}
