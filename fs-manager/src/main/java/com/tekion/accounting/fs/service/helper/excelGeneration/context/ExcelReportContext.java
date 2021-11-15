package com.tekion.accounting.fs.service.helper.excelGeneration.context;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import com.tekion.accounting.fs.service.helper.excelGeneration.dto.ESReportCallbackDto;
import com.tekion.core.excelGeneration.models.model.FetchNextBatchRequest;
import com.tekion.tekionconstant.lookupconsumer.LookupAsset;
import lombok.Data;

import java.util.Map;

@Data
public class ExcelReportContext {

    private FetchNextBatchRequest fetchNextBatchRequest;
    private ESReportCallbackDto esReportCallbackDto = null;

    private Map<LookupAsset, Map<String, JsonNode>> lookupAssetMap = Maps.newHashMap();
}
