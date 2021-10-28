package com.tekion.accounting.fs.memo.dto;

import com.tekion.accounting.fs.memo.beans.MemoValue;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class MemoWorkSheetUpdateDto {
    private String id;
    private List<MemoValue> values;
    private BigDecimal begBalance;
    private Boolean active = true;
}
