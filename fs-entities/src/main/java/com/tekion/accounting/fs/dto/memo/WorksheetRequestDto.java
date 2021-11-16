package com.tekion.accounting.fs.dto.memo;

import com.tekion.accounting.fs.enums.OEM;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorksheetRequestDto {
    @NotNull private OEM oemId;
    @NotNull private Integer version;
    @NotNull private Integer year;
}