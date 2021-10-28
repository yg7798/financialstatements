package com.tekion.accounting.fs.memo.dto;

import com.tekion.accounting.fs.master.enums.OEM;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class MemoWorksheetTemplateBulkRequest {
    @NotNull
    private OEM oem;
    @NotNull
    private Integer year;
    private int version = 1;
    @NotBlank
    private String country;
    @Valid
    @NotEmpty
    private List<MemoWorksheetTemplateRequestDto> memoTemplates;
}
