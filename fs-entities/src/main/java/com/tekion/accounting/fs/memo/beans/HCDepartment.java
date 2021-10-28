package com.tekion.accounting.fs.memo.beans;

import lombok.Data;

import java.util.List;

@Data
public class HCDepartment {
    private String key;
    private String name;
    private int order;
    private String subDepartment;
    private List<String> supportedPositions;
}
