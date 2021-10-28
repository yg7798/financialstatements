package com.tekion.accounting.fs.memo.dto;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class HCBulkUpdateDto {
    @NotNull
    @NotEmpty
    private List<HCUpdateDto> hcWorksheets;
    private String siteId;
}
