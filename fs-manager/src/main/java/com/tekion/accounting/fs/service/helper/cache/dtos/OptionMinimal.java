package com.tekion.accounting.fs.service.helper.cache.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OptionMinimal {
    private String code;
    private String name;
}
