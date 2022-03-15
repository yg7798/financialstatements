package com.tekion.accounting.fs.dto.mappings;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class GroupCodesVsGLAccounts {
    private String groupCode;
    private List<String> glAccounts;

}
