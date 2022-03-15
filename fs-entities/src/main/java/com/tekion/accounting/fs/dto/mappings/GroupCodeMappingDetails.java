package com.tekion.accounting.fs.dto.mappings;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class GroupCodeMappingDetails {
    private String oemId;
    private List<GroupCodesVsGLAccounts> groupCodesMapping;

}
