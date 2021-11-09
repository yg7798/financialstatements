package com.tekion.accounting.fs.excelGeneration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tekion.as.models.dto.TekTextSearchAndAggregationRequest;
import com.tekion.core.excelGeneration.models.model.Sort;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EsReportRequestDto {
    private TekTextSearchAndAggregationRequest searchRequest;
    private List<Sort> sortList;

}
