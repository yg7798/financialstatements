package com.tekion.accounting.fs.memo.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemoValue implements Comparable<MemoValue>{

    //month range {1..12}
    private int month;
    private BigDecimal mtdValue = BigDecimal.ZERO;
    private BigDecimal ytdValue = BigDecimal.ZERO;

    @Override
    public int compareTo(MemoValue o) {
        return this.month - o.month;
    }
}
