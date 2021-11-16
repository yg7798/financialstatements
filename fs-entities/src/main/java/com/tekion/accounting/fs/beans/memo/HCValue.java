package com.tekion.accounting.fs.beans.memo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class HCValue implements Comparable<HCValue>{
    private int month;
    private BigDecimal value = BigDecimal.ZERO;

    @Override
    public int compareTo(HCValue o) {
        return this.month - o.month;
    }
}

