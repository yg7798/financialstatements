package com.tekion.accounting.fs.dto.fsEntry;

import com.tekion.accounting.fs.beans.common.FSEntry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FSEntryUpdateDto {

    @NotEmpty
    private String id;
    @NotEmpty
    @Size(max = FSEntry.NAME_MAX_LENGTH, message = "name size cannot exceed 64 characters")
    private String name;
}
