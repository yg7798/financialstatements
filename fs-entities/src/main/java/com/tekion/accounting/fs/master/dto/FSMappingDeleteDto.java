package com.tekion.accounting.fs.master.dto;

import com.tekion.accounting.fs.master.enums.OEM;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FSMappingDeleteDto {
    Collection<String> groupDisplayNames = new ArrayList<>();
    int year;
    @NotNull
    OEM oemId;
    int version;
    String siteId;
}
