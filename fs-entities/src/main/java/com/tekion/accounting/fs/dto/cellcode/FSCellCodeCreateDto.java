package com.tekion.accounting.fs.dto.cellcode;


import com.tekion.accounting.fs.beans.common.AccountingOemFsCellCode;
import com.tekion.accounting.fs.enums.*;
import com.tekion.accounting.fs.common.utils.OemFSUtils;
import com.tekion.core.utils.TStringUtils;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@Data
public class FSCellCodeCreateDto {

    @NotNull private OEM oemId;
    @NotNull private String displayName;
    @NotBlank private String code;
    @NotNull private Integer year;
    @NotNull private Integer version;
    @NotBlank private String country;
    private boolean derived;
    private OemCellSubType subType;
    private OemCellValueType valueType;
    private OemCellDurationType durationType;
    @NotNull private String expression;
    private String groupCode;
    private Map<String, String> additionalInfo = new HashMap<>();
    private String oemCode;
    private String source;
    private String oemDescription;
    private String oemValueType;  // can be value, unit1, unit2, amount1....etc
    private Map<String, String> tags = new HashMap<>();


    public AccountingOemFsCellCode toFsCellCode() {
        AccountingOemFsCellCode fsCellCode = new AccountingOemFsCellCode();
        fsCellCode.setOemId(oemId.name());
        fsCellCode.setDisplayName(displayName);
        fsCellCode.setCode(code);
        fsCellCode.setYear(year);
        fsCellCode.setVersion(version);
        fsCellCode.setCountry(country);
        fsCellCode.setDerived(derived);
        fsCellCode.setSubType(subType != null ? subType.name() : OemCellSubType.BASIC.name());
        fsCellCode.setValueType(valueType!=null ? valueType.name() : null);
        fsCellCode.setDurationType(durationType!=null ? durationType.name(): null);
        fsCellCode.setExpression(expression);
        fsCellCode.setDependentFsCellCodes(OemFSUtils.getCodesFromExpression(expression));
        fsCellCode.setGroupCode(groupCode);
        fsCellCode.setAdditionalInfo(additionalInfo);
        fsCellCode.setOemCode(oemCode);
        fsCellCode.setTags(tags);
        if(TStringUtils.isNotBlank(source)){
            fsCellCode.setSource(FsCellCodeSource.valueOf(source));
        }
        fsCellCode.setOemValueType(oemValueType);
        fsCellCode.setOemDescription(oemDescription);
//        fsCellCode.setDealerId(UserContextProvider.getCurrentDealerId());
//        fsCellCode.setCreatedByUserId(UserContextProvider.getCurrentUserId());
//        fsCellCode.setModifiedByUserId(UserContextProvider.getCurrentUserId());
        fsCellCode.setCreatedTime(System.currentTimeMillis());
        fsCellCode.setModifiedTime(System.currentTimeMillis());

        OemValueType.validate(oemValueType);
        return fsCellCode;
    }


    public void applyFieldsToExistingFsCellCode(AccountingOemFsCellCode fsCellCodeFromDb) {
        fsCellCodeFromDb.setDisplayName(this.displayName);
        fsCellCodeFromDb.setDerived(this.derived);
        fsCellCodeFromDb.setValueType(this.valueType!=null ? this.valueType.name() : null);
        fsCellCodeFromDb.setSubType(this.subType != null ? this.subType.name() : OemCellSubType.BASIC.name());
        fsCellCodeFromDb.setDurationType(this.durationType!=null ? this.durationType.name() : null);
        fsCellCodeFromDb.setExpression(this.expression);
        fsCellCodeFromDb.setDependentFsCellCodes(OemFSUtils.getCodesFromExpression(this.expression));
        fsCellCodeFromDb.setGroupCode(this.groupCode);
        fsCellCodeFromDb.setAdditionalInfo(this.additionalInfo);
        fsCellCodeFromDb.setOemCode(oemCode);
        if(TStringUtils.isNotBlank(source)){
            fsCellCodeFromDb.setSource(FsCellCodeSource.valueOf(source));
        }
//        fsCellCodeFromDb.setModifiedByUserId(UserContextProvider.getCurrentUserId());
        fsCellCodeFromDb.setModifiedTime(System.currentTimeMillis());

    }

}

