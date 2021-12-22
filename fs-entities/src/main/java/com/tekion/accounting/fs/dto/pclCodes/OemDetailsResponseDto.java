package com.tekion.accounting.fs.dto.pclCodes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OemDetailsResponseDto {
    private String oemId;
    private Integer year;
    private String country;
}
