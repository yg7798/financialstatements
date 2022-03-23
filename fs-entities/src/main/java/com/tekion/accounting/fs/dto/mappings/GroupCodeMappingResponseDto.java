package com.tekion.accounting.fs.dto.mappings;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class GroupCodeMappingResponseDto {
    private Integer year;
    private String oemId;
    private List<GroupCodesVsGLAccounts> groupCodesMapping;

}
