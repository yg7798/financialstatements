package com.tekion.accounting.fs.service.common.excelGeneration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tekion.as.models.dto.TekTextSearchAndAggregationRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ESReportCallbackDto extends AccReportCallbackDto {

    private TekTextSearchAndAggregationRequest searchRequestToUse; //withFilterAttached //originalFilterList + //attachedByUs
    private String targetDealerId;
    private Object extraInfoForCallback;
}
