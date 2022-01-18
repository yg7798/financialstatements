package com.tekion.accounting.fs.dto.pclCodes;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class MediaRequestDto {
    @NotNull
    String mediaId;
}
