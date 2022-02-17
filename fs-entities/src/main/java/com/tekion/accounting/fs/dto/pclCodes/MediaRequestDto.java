package com.tekion.accounting.fs.dto.pclCodes;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class MediaRequestDto {
    String mediaId;
    @NotNull
    PclCodeEnum dmsType;
    @NotNull
    String oemId;
    @NotNull
    Integer year;
    @NotNull
    String country;
    @NotNull
    String preSignedUrl;
}
