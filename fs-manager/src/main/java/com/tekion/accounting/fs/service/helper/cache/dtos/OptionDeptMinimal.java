package com.tekion.accounting.fs.service.helper.cache.dtos;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OptionDeptMinimal extends OptionMinimal{
    private String deptType;
}
