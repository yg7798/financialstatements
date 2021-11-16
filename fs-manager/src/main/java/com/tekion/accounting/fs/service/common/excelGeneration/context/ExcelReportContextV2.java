package com.tekion.accounting.fs.service.common.excelGeneration.context;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import com.tekion.core.excelGeneration.models.model.v2.FetchNextBatchRequestV2;
import com.tekion.tekionconstant.lookupconsumer.LookupAsset;
import lombok.Data;

import java.util.Map;

@Data
public class ExcelReportContextV2 {
    private FetchNextBatchRequestV2 nextBatchRequestV2;
    private Map<LookupAsset, Map<String, JsonNode>> lookupAssetMap = Maps.newHashMap();
}
