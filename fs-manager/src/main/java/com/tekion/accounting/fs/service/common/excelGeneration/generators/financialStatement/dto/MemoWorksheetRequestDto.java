package com.tekion.accounting.fs.service.common.excelGeneration.generators.financialStatement.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tekion.core.excelGeneration.models.model.Sort;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemoWorksheetRequestDto {
    @NotBlank
    private String fsId;
    private int month;
    private List<Sort> sortList ;
    private List<WorksheetApplicableFilter> applicableFilters ;
    private String searchText;
    private List<String> searchableFields;
    private boolean showEmptyValues=true;
}
