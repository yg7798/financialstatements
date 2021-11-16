package com.tekion.accounting.fs.dto.mappings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OemFsMappingDetails {

    private String glAccountId;
    private BigDecimal balanceYtd;
    private BigDecimal balanceMtd;
    private Long countYtd;
    private Long countMtd;
    private String fsCellCode;
}
