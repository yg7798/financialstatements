package com.tekion.accounting.fs.dto.oemTemplate;

import com.tekion.accounting.fs.enums.OEM;
import com.tekion.tekionconstant.locale.TekLocale;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateDetail {
    @NotNull private OEM oemId;
    @NotNull private Integer year;
    private Integer version;
    @NotBlank
    private String country;
    @NotNull
    TekLocale locale;
    private boolean active;
    private Object template;
}
