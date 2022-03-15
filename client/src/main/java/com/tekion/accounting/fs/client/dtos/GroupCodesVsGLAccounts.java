package com.tekion.accounting.fs.client.dtos;

import lombok.Data;

import java.util.List;

@Data
public class GroupCodesVsGLAccounts {
    private String groupCode;
    private List<String> glAccounts;

}
