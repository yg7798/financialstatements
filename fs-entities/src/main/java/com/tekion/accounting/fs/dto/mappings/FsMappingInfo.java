package com.tekion.accounting.fs.dto.mappings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FsMappingInfo {

    private String id;
    private String oemId;
    private Integer year;
    private Integer version;
    private String fsType;
    private String siteId;
    private String name;
    private Integer mappedAccounts;
    private Integer unmappedAccounts;
    private String modifiedByUserId;
    private Long lastModifiedTime;
    private List<String> dealerIds;
    private String tenantId;
}