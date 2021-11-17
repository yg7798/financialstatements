package com.tekion.accounting.fs.dto.cellcode;

import com.tekion.accounting.fs.enums.OEM;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Data
public class OemCodeUpdateDto {
    @NotNull
    private OEM oemId;
    @NotNull
    private Integer year;
    @NotBlank
    private String country;
    @Valid
    @NotEmpty
    List<CodeUpdate> codes;

    @Data
    public static class CodeUpdate{
        @NotEmpty
        private String code;
        private String durationType;
        private String oemCode;
        private Map<String, String> additionalInfo;
    }
}
