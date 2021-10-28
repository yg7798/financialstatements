package com.tekion.accounting.fs.memo.dto;

import com.tekion.accounting.fs.master.enums.OEM;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CopyMemoValuesDto {
    @NotNull
    private OEM oemId;
    private Integer version;
    private Integer fromYear;
    private Integer fromMonth;
    private Integer toYear;
    private Integer toMonth;
    private boolean copyAllValues;
    @Builder.Default
    private List<String> keys = new ArrayList<>();
}
