package com.tekion.accounting.fs.dto.memo;

import com.tekion.accounting.fs.beans.memo.FieldType;
import com.tekion.accounting.fs.beans.memo.MemoWorksheetTemplate;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.enums.OemCellDurationType;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.TStringUtils;
import com.tekion.core.utils.UserContextProvider;
import com.tekion.multilingual.dto.TekMultiLingualBean;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Set;

@Data
public class MemoWorksheetTemplateRequestDto {
    private String id;
//    @NotNull
    private OEM oem;
//    @NotNull
    private Integer year;
    private int version = 1;
    private String country;
    @NotBlank
    private String name;
    private String key;
    private Set<OemCellDurationType> durationTypes;
    @NotBlank
    private String lineNumber;
    @NotBlank
    private String pageNumber;
    private Map<String,String> additionalInfo;
    @NotNull
    private FieldType fieldType;
    private TekMultiLingualBean languages;

    public MemoWorksheetTemplate toMemoWorksheetTemplate(){
        MemoWorksheetTemplate memoWorksheetTemplate = new MemoWorksheetTemplate();
        memoWorksheetTemplate.setName(getName());
        memoWorksheetTemplate.setYear(getYear());
        memoWorksheetTemplate.setVersion(getVersion());
        memoWorksheetTemplate.setCountry(getCountry());
        memoWorksheetTemplate.setDurationTypes(getDurationTypes());
        memoWorksheetTemplate.setOemId(getOem().name());
        memoWorksheetTemplate.setKey(TStringUtils.isBlank(getKey())?constructKey():getKey());
        memoWorksheetTemplate.setCreatedTime(System.currentTimeMillis());
        memoWorksheetTemplate.setModifiedTime(System.currentTimeMillis());
        memoWorksheetTemplate.setCreatedByUserId(UserContextProvider.getCurrentUserId());
        memoWorksheetTemplate.setId(this.getId());
        memoWorksheetTemplate.setLineNumber(this.getLineNumber());
        memoWorksheetTemplate.setPageNumber(this.getPageNumber());
        memoWorksheetTemplate.setAdditionalInfo(TCollectionUtils.nullSafeMap(this.getAdditionalInfo()));
        memoWorksheetTemplate.setFieldType(this.getFieldType());
        memoWorksheetTemplate.setLanguages(this.getLanguages());
        return memoWorksheetTemplate;
    }

    private String constructKey() {
        return getName().trim().replace(" ","_").replace('-','_').replace('&','_')
                .replace('(','_').replace(')','_').replace("__","_");
    }
}
