package com.tekion.accounting.fs.dto.pclCodes;

import lombok.*;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class PclDownloadRequestDto {
    @NotNull
    private String oemId;
    @NotNull
    private Integer year;
    @NotNull
    private String country;
    @NotNull
    private PclCodeEnum dmsType;
}
