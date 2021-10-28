package com.tekion.accounting.fs.master.dto;

import com.tekion.accounting.fs.master.beans.AccountingOemFsCellCode;
import com.tekion.accounting.fs.master.enums.FsCellCodeSource;
import com.tekion.accounting.fs.master.enums.OEM;
import com.tekion.accounting.fs.utils.OemFSUtils;
import com.tekion.core.utils.TStringUtils;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

@Data
public class FSCellCodeListCreateDto {

    @NotNull
    private Integer year;
    @NotNull
    private Integer version;
    @NotNull
    private OEM oemId;
    @NotBlank
    private String country;
    @Valid
    private List<FSCellCodeInfoRequest> fsCellCodeDetails;


    public AccountingOemFsCellCode toFsCellCode(FSCellCodeInfoRequest fsCellCodeInfo) {
        AccountingOemFsCellCode fsCellCode = new AccountingOemFsCellCode();
        fsCellCode.setOemId(this.oemId.name());
        fsCellCode.setDisplayName(fsCellCodeInfo.getDisplayName());
        fsCellCode.setCode(fsCellCodeInfo.getCode());
        fsCellCode.setGroupCode(fsCellCodeInfo.getGroupCode());
        fsCellCode.setAdditionalInfo(fsCellCodeInfo.getAdditionalInfo());
        fsCellCode.setYear(this.year);
        fsCellCode.setVersion(this.version);
        fsCellCode.setCountry(this.country);
        fsCellCode.setDerived(fsCellCodeInfo.isDerived());
        fsCellCode.setSubType(fsCellCodeInfo.getSubType() != null ? fsCellCodeInfo.getSubType().name() : OemCellSubType.BASIC.name());
        fsCellCode.setValueType(fsCellCodeInfo.getValueType()!=null ? fsCellCodeInfo.getValueType().name() : null);
        fsCellCode.setDurationType(fsCellCodeInfo.getDurationType()!=null ? fsCellCodeInfo.getDurationType().name(): null);
        fsCellCode.setExpression(fsCellCodeInfo.getExpression());
        fsCellCode.setDependentFsCellCodes(OemFSUtils.getCodesFromExpression(fsCellCodeInfo.getExpression()));
        fsCellCode.setOemCode(fsCellCodeInfo.getOemCode());
        fsCellCode.setCreatedTime(System.currentTimeMillis());
        fsCellCode.setModifiedTime(System.currentTimeMillis());
        fsCellCode.setTags(fsCellCodeInfo.getTags());
        fsCellCode.setOemDescription(fsCellCodeInfo.getOemDescription());

        if(Objects.nonNull(fsCellCodeInfo.getOemValueType())){
            fsCellCode.setOemValueType(fsCellCodeInfo.getOemValueType().name());
        }

        if(TStringUtils.isNotBlank(fsCellCodeInfo.getSource())){
            fsCellCode.setSource(FsCellCodeSource.valueOf(fsCellCodeInfo.getSource()));
        }

        return fsCellCode;
    }



    public void applyFieldsToExistingFsCellCode(AccountingOemFsCellCode fsCellCodeFromDb, FSCellCodeInfoRequest fsCellCodeInfo) {
        fsCellCodeFromDb.setDisplayName(fsCellCodeInfo.getDisplayName());
        fsCellCodeFromDb.setDerived(fsCellCodeInfo.isDerived());
        fsCellCodeFromDb.setValueType(fsCellCodeInfo.getValueType()!=null ? fsCellCodeInfo.getValueType().name() : null);
        fsCellCodeFromDb.setDurationType(fsCellCodeInfo.getDurationType()!=null ? fsCellCodeInfo.getDurationType().name() : null);
        fsCellCodeFromDb.setSubType(fsCellCodeInfo.getSubType()!=null ? fsCellCodeInfo.getSubType().name() : OemCellSubType.BASIC.name());
        fsCellCodeFromDb.setExpression(fsCellCodeInfo.getExpression());
        fsCellCodeFromDb.setDependentFsCellCodes(OemFSUtils.getCodesFromExpression(fsCellCodeInfo.getExpression()));
//        fsCellCodeFromDb.setModifiedByUserId(UserContextProvider.getCurrentUserId());
        fsCellCodeFromDb.setGroupCode(fsCellCodeInfo.getGroupCode());
        fsCellCodeFromDb.setAdditionalInfo(fsCellCodeInfo.getAdditionalInfo());
        fsCellCodeFromDb.setOemCode(fsCellCodeInfo.getOemCode());
        fsCellCodeFromDb.setTags(fsCellCodeInfo.getTags());
        fsCellCodeFromDb.setOemDescription(fsCellCodeInfo.getOemDescription());

        if(Objects.nonNull(fsCellCodeInfo.getOemValueType())){
            fsCellCodeFromDb.setOemValueType(fsCellCodeInfo.getOemValueType().name());
        }

        if(TStringUtils.isNotBlank(fsCellCodeInfo.getSource())){
            fsCellCodeFromDb.setSource(FsCellCodeSource.valueOf(fsCellCodeInfo.getSource()));
        }
        fsCellCodeFromDb.setModifiedTime(System.currentTimeMillis());
    }
}
