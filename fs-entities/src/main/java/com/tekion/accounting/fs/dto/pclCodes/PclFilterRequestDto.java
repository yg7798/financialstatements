package com.tekion.accounting.fs.dto.pclCodes;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class PclFilterRequestDto {
    private String oemId;
    private Integer year;
    private String country;
    private PclFilters filters;
}
