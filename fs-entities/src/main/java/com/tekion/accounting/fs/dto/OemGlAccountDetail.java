package com.tekion.accounting.fs.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OemGlAccountDetail {
    private String glAccountId;
    private BigDecimal value = BigDecimal.ZERO;
}
