package com.tekion.accounting.fs.client.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class GroupCodeMappingDetailsDto {
    private String oemId;
    private List<GroupCodesVsGLAccounts> groupCodesMapping;

}
