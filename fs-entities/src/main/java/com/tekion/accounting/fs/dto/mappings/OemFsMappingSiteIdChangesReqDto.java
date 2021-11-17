package com.tekion.accounting.fs.dto.mappings;

import lombok.Data;

@Data
public class OemFsMappingSiteIdChangesReqDto{
    private String oemId;
    private int year;
    private int version;
    private String siteId;
}
