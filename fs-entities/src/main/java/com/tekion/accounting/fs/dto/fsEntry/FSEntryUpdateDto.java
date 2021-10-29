package com.tekion.accounting.fs.dto.fsEntry;

import com.tekion.accounting.fs.beans.FSEntry;
import com.tekion.core.utils.UserContextProvider;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class FSEntryUpdateDto {

    @NotNull
    private Integer year;

    public FSEntry updateFsMappingInfo(FSEntry fsEntry) {
        fsEntry.setYear(year);
        fsEntry.setModifiedTime(System.currentTimeMillis());
        fsEntry.setModifiedByUserId(UserContextProvider.getCurrentUserId());
        return fsEntry;
    }
}
