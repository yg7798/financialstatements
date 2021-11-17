package com.tekion.accounting.fs.service.common.excelGeneration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tekion.accounting.fs.common.core.minimisedResource.MinimizedResourceMetaData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccReportCallbackDto {
    protected MinimizedResourceMetaData minimizedResourceMetaData;
}
