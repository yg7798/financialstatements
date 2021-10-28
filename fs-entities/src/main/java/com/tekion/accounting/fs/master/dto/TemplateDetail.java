package com.tekion.accounting.fs.master.dto;

import com.tekion.accounting.fs.master.enums.OEM;
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
    private boolean active;
    private Object template;
}
