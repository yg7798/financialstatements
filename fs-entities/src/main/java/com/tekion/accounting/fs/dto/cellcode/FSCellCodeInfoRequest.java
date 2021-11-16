package com.tekion.accounting.fs.dto.cellcode;

import com.tekion.accounting.fs.enums.OemCellDurationType;
import com.tekion.accounting.fs.enums.OemCellValueType;
import com.tekion.accounting.fs.enums.OemValueType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@Data
public class FSCellCodeInfoRequest {

    @NotNull private String displayName;
    @NotBlank private String code;
    private boolean derived;
    private OemCellSubType subType;
    private OemCellValueType valueType;
    private OemCellDurationType durationType;
    @NotNull private String expression;
    private String groupCode;
    private String oemCode;
    private String source;
    private String oemDescription;
    private OemValueType oemValueType;
    private Map<String, String> additionalInfo = new HashMap<>();
    private Map<String, String> tags = new HashMap<>();
}