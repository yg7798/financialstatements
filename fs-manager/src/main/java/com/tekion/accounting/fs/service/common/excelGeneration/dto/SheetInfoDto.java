package com.tekion.accounting.fs.service.common.excelGeneration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tekion.accounting.fs.common.core.minimisedResource.MinimizedResourceMetaData;
import com.tekion.core.excelGeneration.models.model.ColumnConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class SheetInfoDto {

    private String sheetIdentifier;
    private MinimizedResourceMetaData minimizedResourceMetaData;
    private List<ColumnConfig> computedColumnConfigList;

}
