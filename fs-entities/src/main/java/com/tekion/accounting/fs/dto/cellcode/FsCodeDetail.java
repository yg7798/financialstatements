package com.tekion.accounting.fs.dto.cellcode;

import com.tekion.accounting.fs.dto.OemGlAccountDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FsCodeDetail {
    @Builder.Default private BigDecimal value = BigDecimal.ZERO; // value we show in UI
    @Builder.Default private BigDecimal originalValue = BigDecimal.ZERO; // Value without roundOff
    @Builder.Default private BigDecimal valueAfterOffset = BigDecimal.ZERO;
    @Builder.Default private BigDecimal roundedValue = BigDecimal.ZERO; // this is based on rounding properties
    @Builder.Default private List<OemGlAccountDetail> glAccountDetails = new ArrayList<>();
    @Builder.Default private List<String> dependentFsCellCodes = new ArrayList<>();
    @Builder.Default private boolean derived = false;
    private String stringValue;
}
