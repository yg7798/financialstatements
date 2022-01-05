package com.tekion.accounting.fs.dto.pclCodes;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class OemDetailsResponseDto {
    private String oemId;
    private Integer year;
    private String country;
}
