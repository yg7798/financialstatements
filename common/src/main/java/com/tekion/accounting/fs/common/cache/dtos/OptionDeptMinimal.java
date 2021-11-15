package com.tekion.accounting.fs.common.cache.dtos;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OptionDeptMinimal extends OptionMinimal{
    private String deptType;
}
