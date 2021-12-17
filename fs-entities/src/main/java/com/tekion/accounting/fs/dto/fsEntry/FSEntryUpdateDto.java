package com.tekion.accounting.fs.dto.fsEntry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FSEntryUpdateDto {

    @NotNull
    private String id;
    @NotNull
    private String name;
}
