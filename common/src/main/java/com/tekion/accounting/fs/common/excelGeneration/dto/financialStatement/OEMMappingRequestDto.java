package com.tekion.accounting.fs.common.excelGeneration.dto.financialStatement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tekion.accounting.fs.common.excelGeneration.dto.EsReportRequestDto;
import com.tekion.accounting.fs.common.validation.RangeValidatorGroup;
import com.tekion.core.es.common.impl.TekFilterRequest;
import com.tekion.core.excelGeneration.models.model.Sort;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OEMMappingRequestDto extends EsReportRequestDto {

    @NotBlank
    private String fsId;
    @Range(min = 1, max = 12, message = "Month should be in range 1 to 12", groups = {RangeValidatorGroup.class})
    private Integer oemFsMonth;
    private boolean includeM13;
    private boolean addM13BalInDecBalances;
    private List<Sort> sortList ;
    private List<TekFilterRequest> filters;
    private String searchText;
}
