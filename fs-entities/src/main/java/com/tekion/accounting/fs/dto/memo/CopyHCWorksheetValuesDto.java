package com.tekion.accounting.fs.dto.memo;

import com.tekion.accounting.fs.enums.OEM;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CopyHCWorksheetValuesDto {
    @NotNull
    private OEM oemId;
    private Integer version;
    private Integer fromYear;
    private Integer fromMonth;
    private Integer toYear;
    private Integer toMonth;
}
