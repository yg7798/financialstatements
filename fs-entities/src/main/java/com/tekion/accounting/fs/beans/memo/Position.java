package com.tekion.accounting.fs.beans.memo;

import lombok.Data;

@Data
public class Position {
    private String key;
    private String name;
    int order;
    private String groupKey = "DEFAULT";
    private String sectionKey;
}
