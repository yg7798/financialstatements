package com.tekion.as.excelGenearation.columnConfigs.context;


import lombok.Data;

@Data
public class DynamicallyUpdateColumnConfigContext {

    // useful in future if we want to keep it report level
    private String reportType;

    // will be useful in future if they want it configurable per report assetId. we can store overrides per assetId and reportType being the assetType
    private String assetId;

}
